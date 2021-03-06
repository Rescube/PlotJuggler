#ifndef PLOTLEGEND_H
#define PLOTLEGEND_H

#include <QObject>
#include "qwt_plot_legenditem.h"
#include "qwt_plot.h"

class PlotLegend: public QObject, public QwtPlotLegendItem
{
    Q_OBJECT
public:
    PlotLegend(QwtPlot* parent):
        _parent_plot(parent),
        _collapsed(false)
    {
        this->attach(parent);
    }

    QRectF hideButtonRect() const;

    bool processWheelEvent(QWheelEvent* ev);

    const QwtPlotItem *processMousePressEvent(QMouseEvent* mouse_event);

private:

    virtual void draw( QPainter *p,
        const QwtScaleMap &xMap, const QwtScaleMap &yMap,
        const QRectF &rect ) const override;

    virtual void drawLegendData( QPainter *painter,
        const QwtPlotItem *, const QwtLegendData &,
        const QRectF & ) const override;

    QwtPlot* _parent_plot;
    bool _collapsed;

signals:
    void legendSizeChanged(int point_size);
};

#endif // PLOTLEGEND_H
