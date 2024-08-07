
package poljap;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class PoljAp {
  private static final int MAX_PROIZVODA = 4000;
    private static Map<Integer, String> proizvodi = new HashMap<>();
    private static JTextField nazivText;
    private static JTextField sifraText;
    private static JList<String> suggestionList;
    private static JScrollPane suggestionPane;
    private static DefaultListModel<String> suggestionModel;
    private static JWindow suggestionWindow;
    private static JLabel kursLabel;
    private static JTextField evroText;
    private static JLabel dinariLabel;
    
    public static void main(String[] args) {
      // Popunjavanje unapred definisanih proizvoda
        popuniProizvode();

        // Kreiranje glavnog prozora
        JFrame frame = new JFrame("Fiskalna Kasa");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 350);

        // Kreiranje panela za unos i pretragu
        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel);

        // Prikazivanje prozora
        frame.setVisible(true);

        // Preuzimanje kursa
        preuzmiKurs();
    }

    private static void popuniProizvode() {
        proizvodi.put(5, "hrana za kokoske "+"sifra 5");
        proizvodi.put(105, "hrana za bikove "+"sifra 105");
        proizvodi.put(2225, "hrana za pse "+"sifra 2225");
        // Dodajte ostale proizvode prema potrebi
    }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel nazivLabel = new JLabel("Naziv proizvoda:");
        nazivLabel.setBounds(10, 20, 150, 25);
        panel.add(nazivLabel);

        nazivText = new JTextField(20);
        nazivText.setBounds(150, 20, 165, 25);
        panel.add(nazivText);

        // Sugestija padajućeg menija za naziv proizvoda
        suggestionModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionModel);
        suggestionPane = new JScrollPane(suggestionList);
        suggestionWindow = new JWindow();
        suggestionWindow.getContentPane().add(suggestionPane);

        JLabel sifraLabel = new JLabel("Šifra proizvoda:");
        sifraLabel.setBounds(10, 70, 150, 25);
        panel.add(sifraLabel);

        sifraText = new JTextField(20);
        sifraText.setBounds(150, 70, 165, 25);
        panel.add(sifraText);

        JLabel kursInfoLabel = new JLabel("Kurs (1 EUR u RSD):");
        kursInfoLabel.setBounds(10, 120, 150, 25);
        panel.add(kursInfoLabel);

        kursLabel = new JLabel("Preuzimanje kursa...");
        kursLabel.setBounds(150, 120, 165, 25);
        panel.add(kursLabel);

        JLabel evroLabel = new JLabel("Količina evra:");
        evroLabel.setBounds(10, 170, 150, 25);
        panel.add(evroLabel);

        evroText = new JTextField(20);
        evroText.setBounds(150, 170, 165, 25);
        panel.add(evroText);

        JLabel dinariInfoLabel = new JLabel("Vrednost u dinarima:");
        dinariInfoLabel.setBounds(10, 220, 150, 25);
        panel.add(dinariInfoLabel);

        dinariLabel = new JLabel("");
        dinariLabel.setBounds(150, 220, 165, 25);
        panel.add(dinariLabel);

        // Dodavanje DocumentListener-a za tekstualno polje naziv proizvoda
        nazivText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSuggestionsNaziv();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSuggestionsNaziv();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSuggestionsNaziv();
            }

            private void updateSuggestionsNaziv() {
                String input = nazivText.getText();
                suggestionModel.removeAllElements();
                if (!input.isEmpty()) {
                    for (Map.Entry<Integer, String> entry : proizvodi.entrySet()) {
                        if (entry.getValue().toLowerCase().contains(input.toLowerCase())) {
                            suggestionModel.addElement(entry.getValue());
                        }
                    }
                    if (suggestionModel.getSize() > 0) {
                        Point location = nazivText.getLocationOnScreen();
                        suggestionWindow.setLocation(location.x, location.y + nazivText.getHeight());
                        suggestionWindow.setSize(nazivText.getWidth(), 100);
                        suggestionWindow.setVisible(true);
                    } else {
                        suggestionWindow.setVisible(false);
                    }
                } else {
                    suggestionWindow.setVisible(false);
                }
            }
        });

        // Dodavanje DocumentListener-a za tekstualno polje sifra proizvoda
        sifraText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSuggestionsSifra();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSuggestionsSifra();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSuggestionsSifra();
            }

            private void updateSuggestionsSifra() {
                String input = sifraText.getText();
                suggestionModel.removeAllElements();
                if (!input.isEmpty()) {
                    try {
                        int sifra = Integer.parseInt(input);
                        if (proizvodi.containsKey(sifra)) {
                            suggestionModel.addElement(proizvodi.get(sifra));
                        }
                    } catch (NumberFormatException ex) {
                        // Unos nije validan broj, ignorisati
                    }
                    if (suggestionModel.getSize() > 0) {
                        Point location = sifraText.getLocationOnScreen();
                        suggestionWindow.setLocation(location.x, location.y + sifraText.getHeight());
                        suggestionWindow.setSize(sifraText.getWidth(), 50);
                        suggestionWindow.setVisible(true);
                    } else {
                        suggestionWindow.setVisible(false);
                    }
                } else {
                    suggestionWindow.setVisible(false);
                }
            }
        });

        // Dodavanje DocumentListener-a za tekstualno polje evroText
        evroText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDinari();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDinari();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateDinari();
            }

            private void updateDinari() {
                String input = evroText.getText();
                try {
                    double evri = Double.parseDouble(input);
                    String kursText = kursLabel.getText();
                    System.out.println("Kurs (1 EUR u RSD) iz labela: " + kursText);
                    System.out.println("Uneta količina evra: " + evri);
                    if (kursText.equals("N/A")) {
                        dinariLabel.setText("N/A");
                    } else {
                        double kurs = Double.parseDouble(kursText);
                        double dinari = evri * kurs;
                        dinariLabel.setText(String.format("%.2f", dinari));
                        System.out.println("Izračunata vrednost u dinarima: " + dinari);
                    }
                } catch (NumberFormatException ex) {
                    dinariLabel.setText("N/A");
                }
            }
        });
    }

    private static void preuzmiKurs() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    URL url = new URL("https://api.exchangerate-api.com/v4/latest/EUR");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);

                    int status = con.getResponseCode();
                    if (status == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuilder content = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                        in.close();

                        // Logovanje odgovora
                        System.out.println("API Response: " + content.toString());

                        JSONObject obj = new JSONObject(content.toString());
                        if (obj.has("rates")) {
                            JSONObject rates = obj.getJSONObject("rates");
                            double kurs = rates.getDouble("RSD");
                            SwingUtilities.invokeLater(() -> kursLabel.setText(String.valueOf(kurs)));
                        } else {
                            SwingUtilities.invokeLater(() -> kursLabel.setText("N/A"));
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> kursLabel.setText("N/A"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> kursLabel.setText("N/A"));
                }
                return null;
            }
        };
        worker.execute();
    }
    
}
