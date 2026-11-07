package presentation;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import net.miginfocom.swing.MigLayout;
final class KpiCard extends JPanel {
    private static final long serialVersionUID = 1L;
        private final Color corInicio;
        private final Color corFim;
        private final JLabel lblValor;

        KpiCard(String titulo, String valor, String subtitulo, Color corInicio, Color corFim) {
            this.corInicio = corInicio;
            this.corFim = corFim;
            setOpaque(false);
            setLayout(new MigLayout("insets 18 22 18 22, fill, wrap 1"));

            JLabel lblTitulo = new JLabel(titulo.toUpperCase());
            lblTitulo.setForeground(new Color(255, 255, 255, 190));
            lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 11f));

            this.lblValor = new JLabel(valor);
            this.lblValor.setForeground(Color.WHITE);
            this.lblValor.setFont(this.lblValor.getFont().deriveFont(Font.BOLD, 28f));

            JLabel lblSub = new JLabel(subtitulo);
            lblSub.setForeground(Color.decode("#FDB913"));
            lblSub.setFont(lblSub.getFont().deriveFont(Font.PLAIN, 12f));

            add(lblTitulo);
            add(this.lblValor, "gaptop 2");
            add(lblSub, "gaptop 4");
        }

        void setValor(String novoValor) {
            this.lblValor.setText(novoValor);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, corInicio, getWidth(), getHeight(), corFim));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 18, 18));
            g2.dispose();
            super.paintComponent(g);
        }
    }
