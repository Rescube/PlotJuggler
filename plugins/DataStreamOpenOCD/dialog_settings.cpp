#include "dialog_settings.h"
#include "ui_dialog_settings.h"

DialogSettings::DialogSettings(QWidget *parent) :
  QDialog(parent),
  ui(new Ui::DialogSettings)
{
  ui->setupUi(this);
}

DialogSettings::~DialogSettings()
{
  delete ui;
}
