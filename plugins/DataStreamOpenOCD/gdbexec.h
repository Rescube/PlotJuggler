#pragma once

#include <QDebug>
#include <QProcess>
#include <QThread>

class InfoVar;

class GdbExec
{
  QProcess m_p;
  bool verbose = true;

  enum Result {
    IO_ERROR = 0,
    CMD_ERROR = 1,
    CMD_IN_PROGRESS = 2,
    CMD_END_OK = 3,
  };

  enum GdbStatus {
    Idle,
    Error = 1,
    Initializing = 2,
    Initialized = 3
  };

  const QString GDB_PROMPT = "(gdb) ";
  int GDB_PROMPT_SIZE = 6;

  int m_status = 3;
  QString m_stdoutStr = "";
  QString m_stderrStr = "";
  long m_addressOffset = 0L;
  InfoVar *m_getGdbVar = nullptr;
  const QString m_gdbExecutablePath = "/usr/bin/arm-none-eabi-gdb";

public:
  GdbExec();
  ~GdbExec();

  bool isRunning() const;
  bool startGdb();

  QString readAnswer(QProcess::ProcessChannel channel, bool waitforanswer);
  QString getTypeDesc(const QString &  paramString);
  short getType(const QString &  paramString);
  QString getSizeof(const QString &  paramString);
  bool buildVarList(const QString &  symbolFilePath, bool parambool);
  long getSymbolAddress(const QString &  paramString);

private:
  void sendCmd(const QString &  paramString);
  void stop();
  int sendCmdAndWaitAnswer(const QString &  paramString);
};
