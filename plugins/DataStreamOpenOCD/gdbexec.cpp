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
    m_p.readAllStandardError();
    m_p.readAllStandardOutput();
    sendCmd(paramString);

    m_stdoutStr.clear();
    m_stderrStr.clear();
    m_status = WaitForAnswer;
    while (m_status == WaitForAnswer) {
        m_stdoutStr.append(m_p.readAllStandardOutput());
        qWarning() << m_stdoutStr;

        if (m_stdoutStr.endsWith(GDB_PROMPT)) {
            m_status = AnswerReceived;
        } else {
            m_status = WaitForAnswer;
        }

        m_stderrStr = m_p.readAllStandardError();
        if (!m_stderrStr.isEmpty() && verbose) {
            qDebug() << "GDB cmd [" + paramString.trimmed() + "] failed: " + m_stderrStr;
            m_status = Error;
        }

        if (m_status == WaitForAnswer) {
            QCoreApplication::processEvents();
            QThread::msleep(1);
        }
    }
    return m_status;
}
