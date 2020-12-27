#pragma once
#include <QDialog>

#include "ui_datastream_openocd.h"

class DialogSettings;

class OpenOCDDialog : public QDialog
{
  Q_OBJECT

public:
  explicit OpenOCDDialog(QWidget *parent = nullptr);
  ~OpenOCDDialog();

private:
  Ui::DataStreamOpenOCD *ui;
  DialogSettings *m_settingsDialog = nullptr;

private slots:
  void on_toolButtonSettings_clicked();
};

