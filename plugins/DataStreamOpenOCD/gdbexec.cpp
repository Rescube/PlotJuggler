#include "gdbexec.h" 

#include "infovar.h"

#include <QElapsedTimer>

GdbExec::GdbExec()
{
  m_getGdbVar = new InfoVar(this);
}

GdbExec::~GdbExec()
{
  stop();
}

bool GdbExec::isRunning() const
{
  return (m_p.state() == QProcess::Running);
}

bool GdbExec::startGdb()
{
  m_p.start(m_gdbExecutablePath, QStringList());
  if (!m_p.waitForStarted(1000))
    return false;

  QElapsedTimer waitForGdbReturnTimer;
  waitForGdbReturnTimer.start();
  QString stdOut;
  while (waitForGdbReturnTimer.elapsed() < 10000) {
    m_p.write("\n");
    stdOut.append(m_p.readAll().trimmed());
    if (stdOut.endsWith("(gdb)"))
      return true;
    QThread::msleep(10);
  }
  return true;
}

QString GdbExec::readAnswer(QProcess::ProcessChannel channel, bool waitforanswer)
{
  m_p.setReadChannel(channel);
  if (waitforanswer)
    m_p.waitForReadyRead(1000);

  switch (channel) {
  case QProcess::StandardOutput:
    return m_p.readAllStandardOutput();
  case QProcess::StandardError:
    return m_p.readAllStandardError();
  }
  return QString();
}

QString GdbExec::getTypeDesc(const QString &paramString)
{
  int i = 3;
  QString str;

  if (m_p.state() == QProcess::Running) {
    i = sendCmdAndWaitAnswer("ptype " + paramString + "\n");
    if (i == 3)
    {
      if ((m_stdoutStr.startsWith("type = ")) && (m_stdoutStr.endsWith("(gdb) "))) {
        str = QString(m_stdoutStr.mid(7, m_stdoutStr.length() - 6).trimmed());
      }
    }
  }
  return str;
}

short GdbExec::getType(const QString &paramString)
{
  InfoVar localInfoVar(this);
  return localInfoVar.getType(getTypeDesc(paramString), paramString);
}

QString GdbExec::getSizeof(const QString &paramString)
{
  int i = 3;
  QString str = "";

  if (m_p.state() == QProcess::Running) {
    i = sendCmdAndWaitAnswer("p sizeof " + paramString + "\n");
    if (i == 3)
    {
      if ((m_stdoutStr.indexOf("=") != -1) && (m_stdoutStr.endsWith("(gdb) "))) {
        str = QString(m_stdoutStr.mid(m_stdoutStr.indexOf("=") + 1, m_stdoutStr.length() - 6).trimmed());
      }
    }
  }
  return str;
}

bool GdbExec::buildVarList(const QString &symbolFilePath, bool parambool)
{
  int i = 3;
  bool ret = false;

  if (m_p.state() != QProcess::Running) {
    if (!startGdb())
      return false;
  }

  if (m_p.state() == QProcess::Running) {
    if (i == 3) {
      i = sendCmdAndWaitAnswer("symbol-file \"" + symbolFilePath + "\"\n");
    }
    if (i == 3) {
      i = sendCmdAndWaitAnswer("info variables\n");
      if (i == 3) {
        ret = true;
        m_getGdbVar->Parse(m_stdoutStr, parambool);
      }
    }
    stop();
  }
  return ret;
}

long GdbExec::getSymbolAddress(const QString &paramString)
{
  int i = 3;
  long l = -1L;

  i = sendCmdAndWaitAnswer("print /x &(" + paramString + ")\n");
  if (i == 3)
  {
    int j = 0;

    QString str = "= ";
    int k = m_stdoutStr.indexOf(str);
    if (k != -1) {
      int m = k + str.length();

      int n = m_stdoutStr.indexOf("\r", m);
      if (n != -1) {
        l = m_addressOffset + m_stdoutStr.mid(m, n).toLongLong();
        j = 1;
      }
    }
    if ((j == 0) && (verbose)) {
      qWarning() << "Error while extracting address from QString: " + m_stdoutStr + ": set to 0";
    }

  }
  return l;
}

void GdbExec::sendCmd(const QString &paramString)
{
  qWarning() << "sendCmd" << paramString;
  m_p.write(paramString.toLocal8Bit());
}

void GdbExec::stop()
{
  m_p.terminate();
}

int GdbExec::sendCmdAndWaitAnswer(const QString &paramString)
{
  if (!paramString.isEmpty())
  {
    sendCmd(paramString);
    if (m_status == 0) {
      return 0;
    }
  }

  m_status = Initializing;
  m_stdoutStr = "";

  while (m_status == Initializing) {
    m_stdoutStr += readAnswer(QProcess::ProcessChannel::StandardOutput, true);
    if (m_status == 0) {
      return 0;
    }
    if (m_stdoutStr.endsWith("(gdb) ")) {
      qWarning() << m_stdoutStr;
      m_status = Initialized;
    } else {
      m_status = Initializing;
    }

    m_stderrStr = readAnswer(QProcess::ProcessChannel::StandardError, false);
    if (m_status == 0) {
      return 0;
    }

    if (!m_stderrStr.isEmpty() && verbose) {
      qDebug() << "GDB cmd [" + paramString.trimmed() + "] failed: " + m_stderrStr;
      m_status = Error;
    }

    if (m_status != Initialized)
      QThread::msleep(1);
  }
  return m_status;
}
