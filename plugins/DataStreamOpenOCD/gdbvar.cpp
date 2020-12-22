#include "gdbvar.h"

GdbVar::GdbVar(const QString &paramString1, const QString &paramString2)
{
  m_name = paramString1;
  m_filename = paramString2;
  ParseFileDisplayName();
  m_type = -1;
  m_address = 0L;
}

GdbVar::GdbVar(const QString &paramString1, short paramShort, const QString &paramString2, long paramLong)
{
  m_name = paramString1;
  m_filename = paramString2;
  ParseFileDisplayName();
  m_type = paramShort;
  m_address = paramLong;
}

void GdbVar::ParseFileDisplayName()
{
  int i = m_filename.lastIndexOf("\\");
  if (i != -1) {
    QString str = m_filename.mid(0, i);
    m_displayFilename = (m_filename.mid(i + 1) + " (" + str + ")");
  } else {
    m_displayFilename = m_filename;
  }
}

QString GdbVar::get_name() const
{
  return m_name;
}

short GdbVar::get_type() const
{
  return m_type;
}

QString GdbVar::get_filename() const
{
  return m_displayFilename;
}

long GdbVar::get_address()
{
  return m_address;
}

void GdbVar::set_address(long paramLong)
{
  m_address = paramLong;
}
