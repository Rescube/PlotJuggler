#pragma once


#include <QString>

class GdbVar
{
public:
  GdbVar(const QString & paramString1, const QString & paramString2);
  GdbVar(const QString & paramString1, short paramShort, const QString & paramString2, long paramLong);
  QString get_name() const;
  short get_type()  const;
  QString get_filename() const;
  long get_address();
  void set_address(long paramLong);

private:
  QString m_name;
  short m_type;
  QString m_filename;
  QString m_displayFilename;
  long m_address;
  void ParseFileDisplayName();
};
