#include "gdbexec.h" 

#include "infovar.h"

#include <QCoreApplication>
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
  m_p.setProcessChannelMode(QProcess::SeparateChannels);
  m_p.start(m_gdbExecutablePath, QStringList(), QProcess::ReadWrite | QProcess::Unbuffered);

  QElapsedTimer waitForGdbReturnTimer;
  waitForGdbReturnTimer.start();
  QString stdOut;
  while (waitForGdbReturnTimer.elapsed() < 10000) {
    stdOut.append(m_p.readAllStandardOutput());
    if (stdOut.endsWith(GDB_PROMPT)) {
      sendCmdAndWaitAnswer("set pagination off\n");
      return true;
    }
    QCoreApplication::processEvents();
    QThread::msleep(10);
  }
  return false;
}

QString GdbExec::getTypeDesc(const QString &paramString)
{
  GdbCommandResult gdbResult = AnswerReceived;
  QString type;
  if (m_p.state() == QProcess::Running) {
    gdbResult = sendCmdAndWaitAnswer("ptype " + paramString + "\n");
    if (gdbResult == AnswerReceived) {
      if ((m_stdoutStr.startsWith("type = ")) && (m_stdoutStr.endsWith("(gdb) ")))
        type = QString(m_stdoutStr.mid(7, m_stdoutStr.length() - 13).trimmed());
    } else if (gdbResult == Error) {
        qWarning() << "type query failed";
    }
  }
  return type;
}

short GdbExec::getType(const QString &paramString)
{
  InfoVar localInfoVar(this);
  return localInfoVar.getType(getTypeDesc(paramString), paramString);
}

int GdbExec::getSizeof(const QString &paramString)
{
  GdbCommandResult gdbResult = AnswerReceived;
  if (m_p.state() == QProcess::Running) {
    gdbResult = sendCmdAndWaitAnswer("p sizeof " + paramString + "\n");
    if (gdbResult == AnswerReceived) {
      if ((m_stdoutStr.indexOf("=") != -1) && (m_stdoutStr.endsWith(GDB_PROMPT))) {
        auto sizeStart = m_stdoutStr.indexOf("=") + 1;
        return m_stdoutStr.mid(sizeStart, m_stdoutStr.length() - GDB_PROMPT_LENGTH - sizeStart).trimmed().toInt();
      }
    }
  }
  return -1;
}

bool GdbExec::buildVarList(const QString &symbolFilePath, bool parambool)
{
  int i = AnswerReceived;
  bool ret = false;

  if (m_p.state() != QProcess::Running) {
    if (!startGdb())
      return false;
  }

  if (m_p.state() == QProcess::Running) {
    if (i == AnswerReceived)
      i = sendCmdAndWaitAnswer("symbol-file \"" + symbolFilePath + "\"\n");

    if (i == AnswerReceived) {
      i = sendCmdAndWaitAnswer("info variables\n");
      if (i == AnswerReceived) {
        ret = true;
        m_getGdbVar->Parse(m_stdoutStr, parambool);
      }
    }
    stop();
  }
  //m_getGdbVar->dump();
  return ret;
}

long GdbExec::getSymbolAddress(const QString &variable)
{
  int result = AnswerReceived;
  long address = -1L;
  result = sendCmdAndWaitAnswer("print /x &(" + variable + ")\n");
  if (result == AnswerReceived) {
    bool found = false;

    const QString equalString = "= ";
    int addressValuePos = m_stdoutStr.indexOf(equalString);
    if (addressValuePos != -1) {
      int addressStartPos = addressValuePos + equalString.length();
      int lineEndPos = m_stdoutStr.indexOf("\n", addressStartPos);
      if (lineEndPos != -1) {
        bool ok = false;
        address = m_addressOffset + m_stdoutStr.mid(addressStartPos, lineEndPos - addressStartPos).toLongLong(&ok, 16);
        if (ok)
          found = true;
        else
          address = -1;
      }
    }
    if (!found)
      qWarning() << "Error while extracting address from: " + m_stdoutStr + ", address set to 0";
  }
  return address;
}

void GdbExec::dump()
{
  m_getGdbVar->dump();
}

void GdbExec::sendCmd(const QString &command)
{
  m_p.write(command.toLocal8Bit());
}

void GdbExec::stop()
{
  m_p.terminate();
}

GdbExec::GdbCommandResult GdbExec::sendCmdAndWaitAnswer(const QString &command)
{
  GdbExec::GdbCommandResult result = WaitForAnswer;

  m_p.readAllStandardError();
  m_p.readAllStandardOutput();
  m_stdoutStr.clear();
  m_stderrStr.clear();

  sendCmd(command);

  while (result == WaitForAnswer) {
    m_stdoutStr.append(m_p.readAllStandardOutput());
    if (m_stdoutStr.endsWith(GDB_PROMPT)) {
      if (result != Error)
        result = AnswerReceived;
    }

    m_stderrStr.append(m_p.readAllStandardError());
    if (!m_stderrStr.isEmpty()) {
      qDebug() << "GDB cmd [" + command.trimmed() + "] failed: " + m_stderrStr;
      result = Error;
    }

    QCoreApplication::processEvents();
    QThread::msleep(1);
  }
  return result;
}
