#include "gdbvar.h"

#include <stdio.h>
#include <QDebug>

GdbVar::GdbVar(const QString &name, const QString &fileName)
{
  m_name = name;
  m_filename = fileName;
  ParseFileDisplayName();
  m_type = -1;
  m_address = 0L;
}

GdbVar::GdbVar(const QString &name, short paramShort, const QString &fileName, long address)
{
  m_name = name;
  m_filename = fileName;
  ParseFileDisplayName();
  m_type = paramShort;
  m_address = address;
}

void GdbVar::ParseFileDisplayName()
{
  int i = m_filename.lastIndexOf("\\");
  if (i == -1)
    i = m_filename.lastIndexOf("/");

  if (i != -1) {
    QString str = m_filename.left(i);
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

void GdbVar::dump()
{
  printf("%s %s, 0x%lx\n", m_filename.toLocal8Bit().constData(), m_name.toLocal8Bit().constData(), m_address);
  //qWarning() << m_filename << m_name << QString::number(m_address, 16);
}
