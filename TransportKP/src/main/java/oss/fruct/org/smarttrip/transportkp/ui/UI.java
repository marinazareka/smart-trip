package oss.fruct.org.smarttrip.transportkp.ui;

import oss.fruct.org.smarttrip.transportkp.data.Point;
import oss.fruct.org.smarttrip.transportkp.tsp.ClosedStateTransition;
import oss.fruct.org.smarttrip.transportkp.tsp.EuclideanGraphFactory;
import oss.fruct.org.smarttrip.transportkp.tsp.OpenStateTransition;
import oss.fruct.org.smarttrip.transportkp.tsp.TravellingSalesman;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class UI extends JPanel {
	private static final Random random = new Random();
	public static final double PATH_WITHOUT_BREAK = 600;

	public static final int POINT_NORMAL = 1;
	public static final int POINT_STOP = 2;

	public static final int PATH_POINT_GREEN = 4;
	public static final int PATH_POINT_RED = 8;

	public static void main(String[] args) {
		JFrame f = new JFrame("TransportKP");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(new UI());
		f.pack();
		f.setVisible(true);
	}

	private Point start = null;
	private List<Point> pointList = new ArrayList<>();
	private List<Point> stopPointList = new ArrayList<>();
	private List<Point> pathList = new ArrayList<>();

	public UI() {
		setBorder(BorderFactory.createLineBorder(Color.white));
		setFocusable(true);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				super.mouseClicked(mouseEvent);

				switch (mouseEvent.getButton()) {
				case MouseEvent.BUTTON1:
					onMouseLeftClicked(mouseEvent.getX(), mouseEvent.getY());
					break;
				case MouseEvent.BUTTON3:
					onMouseRightClicked(mouseEvent.getX(), mouseEvent.getY());
					break;
				}
			}
		});

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				super.keyPressed(keyEvent);

				switch (keyEvent.getKeyChar()) {
				case 'r':
					onRefresh();

					break;
				case 's':
					onGeneratePoints(stopPointList, POINT_STOP);
					break;

				case 'g':
					onGeneratePoints(pointList, POINT_NORMAL);
					break;
				}
			}
		});
	}

	private void onGeneratePoints(List<Point> points, int mask) {
		for (int i = 0; i < 20; i++) {
			points.add(new Point(mask, random.nextDouble() * getWidth(), random.nextDouble() * getHeight()));
		}
		updatePath();
	}

	private void onRefresh() {
		updatePath();
	}

	private void onMouseRightClicked(int x, int y) {
		stopPointList.add(new Point(POINT_STOP, x, y));

		updatePath();
	}

	private void onMouseLeftClicked(int x, int y) {
		if (start == null) {
			start = new Point(0, x, y);
		} else {
			pointList.add(new Point(POINT_NORMAL, x, y));
		}

		updatePath();
	}

	private void updatePath() {
		ArrayList<Point> res = new ArrayList<>();

		if (!pointList.isEmpty() && start != null) {
			TravellingSalesman tsp = new TravellingSalesman(new EuclideanGraphFactory(),
					new ClosedStateTransition(random),
					pointList.toArray(new Point[pointList.size()]),
					random);

			TravellingSalesman.Result path = tsp.findPath(start, true);
			res.addAll(Arrays.asList(path.points));
		}

		pathList.clear();

		if (!res.isEmpty()) {
			double pathCounter = 0;
			boolean isFailed = false;
			for (int i = 0; i < res.size() - 1; i++) {
				Point point1 = res.get(i);
				Point point2 = res.get(i + 1);
				int colorMask;

				double distanceToPoint2 = point1.distance(point2);

				if (isFailed) {
					colorMask = PATH_POINT_RED;
				} else if (pathCounter + distanceToPoint2 < PATH_WITHOUT_BREAK) {
					pathCounter += distanceToPoint2;
					colorMask = PATH_POINT_GREEN;
				} else {
					Point stopPoint = findNearStopPoint(point1);
					if (stopPoint == null) {
						colorMask = PATH_POINT_RED;
						isFailed = true;
					} else {
						double distanceToStopPoint = point1.distance(stopPoint);
						if (distanceToStopPoint < distanceToPoint2) {
							colorMask = PATH_POINT_GREEN;
							pathCounter = 0;
							res.add(i + 1, stopPoint);
						} else {
							colorMask = PATH_POINT_RED;
							isFailed = true;
						}
					}
				}

				Point newPoint1 = new Point(point1.getId() | colorMask, point1.getLat(), point1.getLon());
				pathList.add(newPoint1);
			}

			pathList.add(res.get(res.size() - 1));
		}

		repaint();
	}

	public Dimension getPreferredSize() {
		return new Dimension(640,480);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = getWidth();
		int h = getHeight();

		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);

		// Draw Text
		g.drawString("This is my custom Panel!", 10, 20);

		if (start != null) {
			g.setColor(Color.red);
			g.fillOval((int) (start.getLat() - 3), (int) (start.getLon() - 3), 6, 6);
		}

		for (Point point : pointList) {
			g.setColor(Color.blue);
			g.fillOval((int) (point.getLat() - 3), (int) (point.getLon() - 3), 6, 6);
		}

		for (Point point : stopPointList) {
			g.setColor(Color.cyan);
			g.fillOval((int) (point.getLat() - 4), (int) (point.getLon() - 4), 8, 8);
		}

		for (int i = 0; i < pathList.size() - 1; i++) {
			Point point1 = pathList.get(i);
			Point point2 = pathList.get(i + 1);

			g.setColor((point1.getId() & PATH_POINT_GREEN) != 0 ? Color.green : Color.red);
			g.drawLine((int) point1.getLat(), (int) point1.getLon(), (int) point2.getLat(), (int) point2.getLon());
		}
	}

	private Point findNearStopPoint(Point point) {
		double minDist = Double.MAX_VALUE;
		Point minPoint = null;

		for (Point stopPoint : stopPointList) {
			double dist = stopPoint.distance(point);
			if (dist < minDist) {
				minPoint = stopPoint;
				minDist = dist;
			}
		}

		return minPoint;
	}
}
