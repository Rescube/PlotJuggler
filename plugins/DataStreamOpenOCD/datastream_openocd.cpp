#include "datastream_openocd.h"
#include "ui_datastream_openocd.h"

#include <QMessageBox>
#include <QDebug>
#include <QSettings>
#include <QDialog>

OpenOCDDialog::OpenOCDDialog(QWidget *parent) :
  QDialog(parent),
  ui(new Ui::DataStreamOpenOCD)
{
  ui->setupUi(this);
}

OpenOCDDialog::~OpenOCDDialog()
{
  delete ui;
}

DataStreamOpenOCD::DataStreamOpenOCD()
{
}

DataStreamOpenOCD::~DataStreamOpenOCD()
{
  shutdown();
}

bool DataStreamOpenOCD::start(QStringList*)
{
  _running = true;
  return _running;
}

void DataStreamOpenOCD::shutdown()
{

}

void DataStreamOpenOCD::receiveLoop()
{
  while( _running )
  {
  }
}


