#include "openocddialog.h"

#include "ui_datastream_openocd.h"
#include "dialog_settings.h"

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

void OpenOCDDialog::on_toolButtonSettings_clicked()
{
  if (m_settingsDialog == nullptr)
    m_settingsDialog = new DialogSettings(this);
  m_settingsDialog->show();
}
