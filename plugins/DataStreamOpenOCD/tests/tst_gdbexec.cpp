#include <QtTest>

#include "../gdbexec.h"

#include <QDebug>

class TestGdbExec : public QObject
{
    Q_OBJECT


private slots:
    void testGdbExec()
    {
        GdbExec e;
        e.buildVarList("/home/mm/Projektek/eagle/targetbot/STM32F103/Debug/targetbot.elf", true);
    }
};


QTEST_APPLESS_MAIN(TestGdbExec)

#include "tst_gdbexec.moc"
