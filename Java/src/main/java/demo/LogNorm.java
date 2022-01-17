package demo;

import smile.stat.distribution.LogNormalDistribution;

class Main {
    public static void main(String[] args) {

        double[] data = new double[] {
            69, 67, 68, 68, 67, 68, 68, 66, 70, 68, 71, 69, 69, 72, 70, 69, 68, 72, 70, 69, 67, 70, 72, 71, 67, 68, 67, 72, 67, 70, 71, 71, 69, 67, 68, 67, 70, 69, 67, 69, 68, 69, 69, 68, 67, 68, 68, 70, 70, 70, 71, 69, 69, 73, 68, 69, 69, 67, 70, 69, 68, 67, 70, 68, 67, 68, 66, 68, 70, 67, 68, 69, 67, 69, 70, 69, 69, 70, 69, 68, 67, 75, 70, 67, 71, 67, 68, 69, 67, 70, 69, 67, 71, 67, 68, 70, 69, 68, 69, 69
        };

        LogNormalDistribution dist = LogNormalDistribution.fit(data);
        dist = new LogNormalDistribution(dist.mu, dist.sigma);
        // LogNormalDistribution dist = new LogNormalDistribution(dist1.mean(), dist1.variance());
        System.out.println(String.valueOf(dist.mean()));
        System.out.println(String.valueOf(dist.variance()));
        System.out.println(String.valueOf(dist.p(65)));
        System.out.println(String.valueOf(dist.length()));

        double[] x = new double[200];
        double[] y = new double[200];

        for (int i = 0; i < 200; i++) {
            x[i] = 60 + (((double)i)/10);
            y[i] = dist.p(x[i]);
        }


        // JAVA:                             // MATLAB:
        Plotter fig = new Plotter(); // figure('Position',[100 100 640 480]);
        fig.plot(x, y, "-r", 2.0f, "AAPL"); // plot(x,y1,'-r','LineWidth',2);
        fig.RenderPlot();                    // First render plot before modifying
        fig.saveas("MyPlot.jpeg",640,480);   // saveas(gcf,'MyPlot','jpeg');
    }
}