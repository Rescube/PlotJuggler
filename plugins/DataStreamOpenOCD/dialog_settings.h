#ifndef DIALOG_SETTINGS_H
#define DIALOG_SETTINGS_H

#include <QDialog>

namespace Ui {
class DialogSettings;
}

class DialogSettings : public QDialog
{
  Q_OBJECT

public:
  explicit DialogSettings(QWidget *parent = nullptr);
  ~DialogSettings();

private:
  Ui::DialogSettings *ui;
};

#endif // DIALOG_SETTINGS_H
