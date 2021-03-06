package ar.edu.itba.imageprocess;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import ar.edu.itba.imageprocess.utils.ArrayUtils;
import ar.edu.itba.imageprocess.utils.ChartUtils;
import ar.edu.itba.imageprocess.utils.Log;
import ar.edu.itba.imageprocess.utils.RandGenerator;

public class Filters {

	public static final int CHART_WIDTH = 400;
	public static final int CHART_HEIGHT = 300;
	public static final int MASK_FILTER_AVERAGE = 1;
	public static final int MASK_FILTER_HIGH_PASS = 2;

	public static Image generateWhiteImage(int width, int height) {
		int[][] grayChannel = new int[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < width; y++) {
				grayChannel[x][y] = 255;
			}
		}
		return new Image(grayChannel);
	}

	public static Image generateCircle(int radius, int imageSize) {
		BufferedImage bufferedImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fill(new Ellipse2D.Double(imageSize / 2 - radius / 2, imageSize / 2 - radius / 2, radius, radius));
		return new Image(bufferedImage);
	}

	public static Image generateSquare(int size, int imageSize) {
		BufferedImage bufferedImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fill(new Rectangle2D.Double(imageSize / 2 - size / 2, imageSize / 2 - size / 2, size, size));
		return new Image(bufferedImage);
	}

	public static Image generateGradient(int imageSize) {
		BufferedImage bufferedImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setPaint(new GradientPaint(0, 0, Color.GRAY, imageSize, 0, Color.WHITE));
		g2d.fill(new Rectangle2D.Double(0, 0, imageSize, imageSize));
		return new Image(bufferedImage);
	}

	public static Image generateColorGradient(int imageSize) {
		BufferedImage bufferedImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setPaint(new GradientPaint(0, 0, Color.BLUE, imageSize, 0, Color.WHITE));
		g2d.fill(new Rectangle2D.Double(0, 0, imageSize, imageSize));
		return new Image(bufferedImage);
	}

	public static Image generateHistogramImage(int[] values) {
		int lowBound = Math.min(0, ArrayUtils.min(values));
		int highBound = Math.max(255, ArrayUtils.max(values));
		return new Image(ChartUtils.createHistogramChartImage(CHART_WIDTH, CHART_HEIGHT, ArrayUtils.intArrayToDoubleArray(values), highBound - lowBound + 1, lowBound, highBound));
	}

	public static Image addImages(Image image1, Image image2) {
		// images must be the same size
		if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) {
			Log.d("images must be the same size");
			return null;
		}

		// prepare the new image channel arrays
		int width = image1.getWidth();
		int height = image1.getHeight();
		int[][] redChannel = new int[width][height];
		int[][] greenChannel = new int[width][height];
		int[][] blueChannel = new int[width][height];

		// add each pixel one by one
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				redChannel[x][y] = image1.getRed(x, y) + image2.getRed(x, y);
				greenChannel[x][y] = image1.getGreen(x, y) + image2.getGreen(x, y);
				blueChannel[x][y] = image1.getBlue(x, y) + image2.getBlue(x, y);
			}
		}

		return new Image(redChannel, greenChannel, blueChannel);
	}

	public static Image subtractImages(Image image1, Image image2) {
		// images must be the same size
		if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) {
			Log.d("images must be the same size");
			return null;
		}

		// prepare the new image channel arrays
		int width = image1.getWidth();
		int height = image1.getHeight();
		int[][] redChannel = new int[width][height];
		int[][] greenChannel = new int[width][height];
		int[][] blueChannel = new int[width][height];

		// subtract each pixel one by one
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				redChannel[x][y] = image1.getRed(x, y) - image2.getRed(x, y);
				greenChannel[x][y] = image1.getGreen(x, y) - image2.getGreen(x, y);
				blueChannel[x][y] = image1.getBlue(x, y) - image2.getBlue(x, y);
			}
		}

		return new Image(redChannel, greenChannel, blueChannel);
	}

	public static Image multiplyScalar(Image image, double scalar) {
		// prepare the new image channel arrays
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] redChannel = new int[width][height];
		int[][] greenChannel = new int[width][height];
		int[][] blueChannel = new int[width][height];

		// multiply each pixel one by one
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				redChannel[x][y] = (int) (image.getRed(x, y) * scalar);
				greenChannel[x][y] = (int) (image.getGreen(x, y) * scalar);
				blueChannel[x][y] = (int) (image.getBlue(x, y) * scalar);
			}
		}

		return new Image(redChannel, greenChannel, blueChannel);
	}

	public static Image compressLinear(Image image) {
		// prepare the new image gray channel
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] grayChannel = image.getGrayChannel();
		int[][] newGrayChannel = new int[width][height];

		// get the bounds and calculate the linear transform parameters
		int[] range = image.getRange(Image.CHANNEL_GRAY);
		double factor = (double) 255 / (range[1] - range[0]);
		double b = -factor * range[0];
		Log.d("factor=" + factor + " b=" + b);

		// apply the filter to all pixels
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				newGrayChannel[x][y] = (int) (grayChannel[x][y] * factor + b);
			}
		}

		return new Image(newGrayChannel);
	}

	public static Image compress(Image image) {
		// prepare the new image gray channel
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] grayChannel = image.getGrayChannel();
		int[][] newGrayChannel = new int[width][height];

		// get the maximum gray level and the factor of compression
		int max = ArrayUtils.max(grayChannel);
		double c = 255 / Math.log(max);

		// apply the filter to all pixels
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				// only apply the filter if the max gray level is above 255
				if (max > 255) {
					newGrayChannel[x][y] = (int) (c * Math.log(grayChannel[x][y] + 1));
				} else {
					newGrayChannel[x][y] = grayChannel[x][y];
				}
				// now apply a simple trim if the gray level is under 0
				newGrayChannel[x][y] = Math.max(0, newGrayChannel[x][y]);
			}
		}

		return new Image(newGrayChannel);
	}

	public static Image filterNegative(Image image) {
		// prepare the new image channel arrays
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] redChannel = new int[width][height];
		int[][] greenChannel = new int[width][height];
		int[][] blueChannel = new int[width][height];

		// inverse the color of each pixel
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				redChannel[x][y] = 255 - image.getRed(x, y);
				greenChannel[x][y] = 255 - image.getGreen(x, y);
				blueChannel[x][y] = 255 - image.getBlue(x, y);
			}
		}

		return new Image(redChannel, greenChannel, blueChannel);
	}

	public static Image filterThreshold(Image image, int threshold) {
		// prepare the new image channel arrays
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] redChannel = new int[width][height];
		int[][] greenChannel = new int[width][height];
		int[][] blueChannel = new int[width][height];

		// apply the threshold to each pixel
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				redChannel[x][y] = image.getRed(x, y) < threshold ? 0 : 255;
				greenChannel[x][y] = image.getGreen(x, y) < threshold ? 0 : 255;
				blueChannel[x][y] = image.getBlue(x, y) < threshold ? 0 : 255;
			}
		}

		return new Image(redChannel, greenChannel, blueChannel);
	}

	public static Image filterContrast(Image image, int r1, int r2, int s1, int s2) {
		// prepare the new image channel arrays
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] grayChannel = new int[width][height];

		// calculate the three linear transforms parameters
		double factor1 = (double) (s1 - 0) / (r1 - 0);
		double b1 = 0;
		double factor2 = (double) (s2 - s1) / (r2 - r1);
		double b2 = -factor2 * r1 + s1;
		double factor3 = (double) (255 - s2) / (255 - r2);
		double b3 = -factor3 * r2 + s2;

		// apply the transforms to each pixels
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int gray = image.getGray(x, y);
				if (gray <= r1) {
					grayChannel[x][y] = (int) (gray * factor1 + b1);
				} else if (gray <= r2) {
					grayChannel[x][y] = (int) (gray * factor2 + b2);
				} else {
					grayChannel[x][y] = (int) (gray * factor3 + b3);
				}
			}
		}

		return new Image(grayChannel);
	}

	public static Image filterEqualize(Image image) {
		// prepare the new image gray channel
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] grayChannel = new int[width][height];

		// let ni be the number of occurrences of gray level i
		int[] ni = image.getHistogram(Image.CHANNEL_GRAY);

		// cumulative frequency distribution
		int[] cuf = new int[ni.length];
		cuf[0] = ni[0];
		for (int i = 1; i < cuf.length; i++) {
			cuf[i] = cuf[i - 1] + ni[i];
		}

		int[] cuFeq = new int[ni.length];
		for (int i = 0; i < cuFeq.length; i++) {
			cuFeq[i] = (int) (i * cuf[cuf.length - 1] / cuf.length);
		}

		int[] output = new int[ni.length];
		for (int i = 0; i < ni.length; i++) {
			output[i] = equalize(cuf[i], cuFeq);
		}

		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				grayChannel[x][y] = (int) (output[image.getGray(x, y)]);
			}
		}

		return new Image(grayChannel);
	}

	public static Image applyAddGaussianNoise(Image image, double spread, double average, double percentage) {
		// prepare the new image gray channel
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] newGrayChannel = new int[width][height];
		int[][] grayChannel = image.getGrayChannel();

		// apply the gaussian noise to each pixel
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double rand = Math.random();
				if (rand <= percentage) {
					newGrayChannel[x][y] = grayChannel[x][y] + (int) (RandGenerator.gaussian(spread, average));
				} else {
					newGrayChannel[x][y] = grayChannel[x][y];
				}
			}
		}

		return new Image(newGrayChannel);
	}

	public static Image applyMulRayleighNoise(Image image, double p, double percentage) {
		// prepare the new image gray channel
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] newGrayChannel = new int[width][height];
		int[][] grayChannel = image.getGrayChannel();

		// apply the rayleigh noise to each pixel
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double rand = Math.random();
				if (rand <= percentage) {
					newGrayChannel[x][y] = (int) (grayChannel[x][y] * (RandGenerator.rayleigh(p)));
				} else {
					newGrayChannel[x][y] = grayChannel[x][y];
				}
			}
		}

		return new Image(newGrayChannel);
	}

	public static Image applyMulExponentialNoise(Image image, double p, double percentage) {
		// prepare the new image gray channel
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] newGrayChannel = new int[width][height];
		int[][] grayChannel = image.getGrayChannel();

		// apply the exponential noise to each pixel
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double rand = Math.random();
				if (rand <= percentage) {
					newGrayChannel[x][y] = (int) (grayChannel[x][y] * (RandGenerator.exponential(p)));
				} else {
					newGrayChannel[x][y] = grayChannel[x][y];
				}
			}
		}

		return new Image(newGrayChannel);
	}

	public static Image generateGaussianChartImage(double spread, double average) {
		// generate test data and create a chart out of it
		int size = 5000;
		double data[] = new double[size];
		for (int i = 0; i < size; i++) {
			data[i] = RandGenerator.gaussian(spread, average);
		}
		return new Image(ChartUtils.createHistogramChartImage(CHART_WIDTH, CHART_HEIGHT, data, 100));
	}

	public static Image generateRayleighChartImage(double param) {
		// generate test data and create a chart out of it
		int size = 5000;
		double data[] = new double[size];
		for (int i = 0; i < size; i++) {
			data[i] = RandGenerator.rayleigh(param);
		}
		return new Image(ChartUtils.createHistogramChartImage(CHART_WIDTH, CHART_HEIGHT, data, 100));
	}

	public static Image generateExponentialChartImage(double param) {
		// generate test data and create a chart out of it
		int size = 5000;
		double data[] = new double[size];
		for (int i = 0; i < size; i++) {
			data[i] = RandGenerator.exponential(param);
		}
		return new Image(ChartUtils.createHistogramChartImage(CHART_WIDTH, CHART_HEIGHT, data, 100));
	}

	public static Image applyPepperAndSalt(Image image, double p0, double p1) {
		// prepare the new image channel arrays
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] redChannel = new int[width][height];
		int[][] greenChannel = new int[width][height];
		int[][] blueChannel = new int[width][height];

		// apply the noise to each pixel of the image
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double rand = Math.random();
				if (rand <= p0) {
					redChannel[x][y] = 0;
					greenChannel[x][y] = 0;
					blueChannel[x][y] = 0;
				} else if (rand >= p1) {
					redChannel[x][y] = 255;
					greenChannel[x][y] = 255;
					blueChannel[x][y] = 255;
				} else {
					redChannel[x][y] = image.getRed(x, y);
					greenChannel[x][y] = image.getGreen(x, y);
					blueChannel[x][y] = image.getBlue(x, y);
				}
			}
		}

		return new Image(redChannel, greenChannel, blueChannel);
	}

	public static Image applyFactorMaskFilter(Image image, int maskWidth, int maskHeight, int filterType) {
		// prepare the new image channel arrays
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] redChannel = new int[width][height];
		int[][] greenChannel = new int[width][height];
		int[][] blueChannel = new int[width][height];

		// get the position of the pixel at the center of the mask
		// if one side has an even length, for example maskWidth = 8
		// the center is considered to be 3 (the fourth column)
		int offsetX = (int) (Math.ceil(maskWidth / 2.0) - 1);
		int offsetY = (int) (Math.ceil(maskHeight / 2.0) - 1);

		// setup the mask and the factor
		double[][] mask = new double[maskWidth][maskHeight];
		double factor = 0;
		if (filterType == MASK_FILTER_AVERAGE) {
			factor = 1.0 / (maskWidth * maskHeight);
			for (int x = 0; x < maskWidth; x++) {
				for (int y = 0; y < maskHeight; y++) {
					mask[x][y] = 1;
				}
			}
		} else if (filterType == MASK_FILTER_HIGH_PASS) {
			factor = 1.0 / (maskWidth * maskHeight);
			for (int x = 0; x < maskWidth; x++) {
				for (int y = 0; y < maskHeight; y++) {
					if (x == offsetX && y == offsetY) {
						mask[x][y] = 8;
					} else {
						mask[x][y] = -1;
					}
				}
			}
		}

		// apply the mask
		applyFactorMask(image, mask, factor, redChannel, greenChannel, blueChannel);

		return new Image(redChannel, greenChannel, blueChannel);
	}

	public static Image applyGaussianMaskFilter(Image image, int maskWidth, int maskHeight, double spread) {
		// prepare the new image channel arrays
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] redChannel = new int[width][height];
		int[][] greenChannel = new int[width][height];
		int[][] blueChannel = new int[width][height];

		// get the position of the pixel at the center of the mask
		// if one side has an even length, for example maskWidth = 8
		// the center is considered to be 3 (the fourth column)
		int offsetX = (int) (Math.ceil(maskWidth / 2.0) - 1);
		int offsetY = (int) (Math.ceil(maskHeight / 2.0) - 1);

		// setup the mask and the factor
		double[][] mask = new double[maskWidth][maskHeight];
		double spread2 = spread * spread;
		double factor = 1.0 / (2 * Math.PI * spread2);
		Log.d("factor=" + factor);

		double total =0;
		for (int x = 0; x < maskWidth; x++) {
			for (int y = 0; y < maskHeight; y++) {
				double dist = (x - offsetX) * (x - offsetX) + (y - offsetY) * (y - offsetY);
				double exp = Math.exp((-1 * dist) / spread2);
				mask[x][y] = exp;
				total+= exp;
				Log.d("val(" + (x - offsetX) + "," + (y - offsetY) + ")=" + (exp));
			}
		}
		

		for (int x = 0; x < maskWidth; x++) {
			for (int y = 0; y < maskHeight; y++) {
				mask[x][y] = mask[x][y] / total;
				Log.d("val(" + (x - offsetX) + "," + (y - offsetY) + ")=" + (mask[x][y]));
			}
		}
		// apply the mask
		applyFactorMask(image, mask, factor, redChannel, greenChannel, blueChannel);

		return new Image(redChannel, greenChannel, blueChannel);
	}

	public static Image applyMedianMaskFilter(Image image, int maskWidth, int maskHeight) {
		// prepare the new image channel arrays
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] redChannel = new int[width][height];
		int[][] greenChannel = new int[width][height];
		int[][] blueChannel = new int[width][height];

		// get the position of the pixel at the center of the mask
		// if one side has an even length, for example maskWidth = 8
		// the center is considered to be 3 (the fourth column)
		int offsetX = (int) (Math.ceil(maskWidth / 2.0) - 1);
		int offsetY = (int) (Math.ceil(maskHeight / 2.0) - 1);

		// apply the mask on each pixel of the image
		for (int pixelX = 0; pixelX < width; pixelX++) {
			for (int pixelY = 0; pixelY < height; pixelY++) {
				int[] redValues = new int[maskWidth * maskHeight];
				int[] greenValues = new int[maskWidth * maskHeight];
				int[] blueValues = new int[maskWidth * maskHeight];

				// get all the pixels values under the mask
				for (int x = 0; x < maskWidth; x++) {
					for (int y = 0; y < maskHeight; y++) {
						redValues[x * maskHeight + y] = image.getRed(pixelX - offsetX + x, pixelY - offsetY + y);
						greenValues[x * maskHeight + y] = image.getGreen(pixelX - offsetX + x, pixelY - offsetY + y);
						blueValues[x * maskHeight + y] = image.getBlue(pixelX - offsetX + x, pixelY - offsetY + y);
					}
				}

				// set the new image pixels to the median
				redChannel[pixelX][pixelY] = (int) (ArrayUtils.median(redValues));
				greenChannel[pixelX][pixelY] = (int) (ArrayUtils.median(greenValues));
				blueChannel[pixelX][pixelY] = (int) (ArrayUtils.median(blueValues));
			}
		}

		return new Image(redChannel, greenChannel, blueChannel);
	}

	public static Image robertsBorderDetection(Image image) {
		// prepare the new image gray channel
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] newGrayChannel = new int[width][height];

		// apply the exponential noise to each pixel
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int gx = image.getGray(x, y) - image.getGray(x + 1, y + 1);
				int gy = image.getGray(x + 1, y) - image.getGray(x, y + 1);
				int gradient = (int) Math.sqrt(gx * gx + gy * gy);
				newGrayChannel[x][y] = gradient;
			}
		}

		return new Image(newGrayChannel);
	}

	public static Image prewittBorderDetection(Image image) {
		// prepare the new image gray channel
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] newGrayChannel = new int[width][height];

		// apply the exponential noise to each pixel
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int gx1 = image.getGray(x, y + 2) + image.getGray(x + 1, y + 2) + image.getGray(x + 2, y + 2);
				int gx2 = image.getGray(x, y) + image.getGray(x + 1, y) + image.getGray(x + 2, y);
				int gx = gx1 - gx2;
				int gy1 = image.getGray(x + 2, y) + image.getGray(x + 2, y + 1) + image.getGray(x + 2, y + 2);
				int gy2 = image.getGray(x, y) + image.getGray(x, y + 1) + image.getGray(x, y + 2);
				int gy = gy1 - gy2;
				int gradient = (int) Math.sqrt(gx * gx + gy * gy);
				newGrayChannel[x][y] = gradient;
			}
		}

		return new Image(newGrayChannel);
	}

	public static Image sobelBorderDetection(Image image) {
		// prepare the new image gray channel
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] newGrayChannel = new int[width][height];

		// apply the exponential noise to each pixel
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int gx1 = image.getGray(x, y + 2) + 2 * image.getGray(x + 1, y + 2) + image.getGray(x + 2, y + 2);
				int gx2 = image.getGray(x, y) + 2 * image.getGray(x + 1, y) + image.getGray(x + 2, y);
				int gx = gx1 - gx2;
				int gy1 = image.getGray(x + 2, y) + 2 * image.getGray(x + 2, y + 1) + image.getGray(x + 2, y + 2);
				int gy2 = image.getGray(x, y) + 2 * image.getGray(x, y + 1) + image.getGray(x, y + 2);
				int gy = gy1 - gy2;
				int gradient = (int) Math.sqrt(gx * gx + gy * gy);
				newGrayChannel[x][y] = gradient;
			}
		}

		return new Image(newGrayChannel);
	}

	/**
	 * Used to find the correct output value for the equalization
	 * 
	 * @return
	 */
	private static int equalize(int n, int[] cuFeq) {
		int min = Math.abs(n - cuFeq[0]);
		int minindex = 0;
		for (int i = 1; i < cuFeq.length; i++) {
			if (Math.abs(n - cuFeq[i]) < min) {
				min = Math.abs(n - cuFeq[i]);
				minindex = i;
			}
		}
		return minindex;
	}

	private static void applyFactorMask(Image image, double[][] mask, double factor, int[][] redChannel, int[][] greenChannel, int[][] blueChannel) {
		int width = image.getWidth();
		int height = image.getHeight();
		int maskWidth = mask.length;
		int maskHeight = mask[0].length;
		int offsetX = (int) (Math.ceil(maskWidth / 2.0) - 1);
		int offsetY = (int) (Math.ceil(maskHeight / 2.0) - 1);

		for (int pixelX = 0; pixelX < width; pixelX++) {
			for (int pixelY = 0; pixelY < height; pixelY++) {
				double redSum = 0;
				double greenSum = 0;
				double blueSum = 0;

				// iterate over the mask for that pixel
				for (int x = 0; x < maskWidth; x++) {
					for (int y = 0; y < maskHeight; y++) {
						redSum += mask[x][y] * image.getRed(pixelX - offsetX + x, pixelY - offsetY + y);
						greenSum += mask[x][y] * image.getGreen(pixelX - offsetX + x, pixelY - offsetY + y);
						blueSum += mask[x][y] * image.getBlue(pixelX - offsetX + x, pixelY - offsetY + y);
					}
				}

				// set the new image pixels
				redChannel[pixelX][pixelY] = (int) (redSum * factor);
				greenChannel[pixelX][pixelY] = (int) (greenSum * factor);
				blueChannel[pixelX][pixelY] = (int) (blueSum * factor);
			}
		}
	}
}
