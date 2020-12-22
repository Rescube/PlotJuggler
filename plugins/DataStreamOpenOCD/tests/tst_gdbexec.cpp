#include <QtTest>

#include "../gdbexec.h"

#include <QDebug>
#include <QEventLoop>

class TestGdbExec : public QObject
{
    Q_OBJECT


private slots:
    void testGdbExec()
    {
        GdbExec e;
        e.buildVarList("/home/mm/Projektek/xlineusb/Debug/xlineusb.elf", true);
    }
};


QTEST_GUILESS_MAIN(TestGdbExec)

#include "tst_gdbexec.moc"
