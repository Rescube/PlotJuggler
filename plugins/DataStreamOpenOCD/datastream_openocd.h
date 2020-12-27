#pragma once
#include <QDialog>

#include "PlotJuggler/datastreamer_base.h"
#include "PlotJuggler/messageparser_base.h"
#include "ui_datastream_openocd.h"

#include <QtPlugin>
#include <thread>

class DataStreamOpenOCD : public PJ::DataStreamer
{
  Q_OBJECT
  Q_PLUGIN_METADATA(IID "facontidavide.PlotJuggler3.DataStreamer")
  Q_INTERFACES(PJ::DataStreamer)

public:
  DataStreamOpenOCD();

  virtual ~DataStreamOpenOCD() override;

  virtual bool start(QStringList*) override;

  virtual void shutdown() override;

  virtual bool isRunning() const override
  {
    return _running;
  }

  virtual const char* name() const override
  {
    return "openOCD BDM data source";
  }

  virtual bool isDebugPlugin() override
  {
    return false;
  }

private:
  void receiveLoop();

  bool _running = false;
};
