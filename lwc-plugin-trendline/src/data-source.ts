import {
	IChartApi,
	ISeriesApi,
	SeriesOptionsMap,
	Time,
} from 'lightweight-charts';
import { TrendLinePluginOptions } from './options';

export interface Point {
	time: Time;
	price: number;
}

export interface TrendLinePluginDataSource {
	chart: IChartApi;
	series: ISeriesApi<keyof SeriesOptionsMap>;
	options: TrendLinePluginOptions;
	p1: Point;
	p2: Point;
}
