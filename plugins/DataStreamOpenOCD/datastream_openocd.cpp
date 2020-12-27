#include "datastream_openocd.h"

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



