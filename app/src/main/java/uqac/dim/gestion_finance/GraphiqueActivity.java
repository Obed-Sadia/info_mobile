package uqac.dim.gestion_finance;

import android.database.Cursor;
import android.graphics.Color;

import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.Calendar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GraphiqueActivity {

    private SupportSQLiteDatabase db;

    public GraphiqueActivity(SupportSQLiteDatabase db) {
        this.db = db;
    }

    // Configuration du graphique circulaire (PieChart) pour un utilisateur spécifique et un mois donné
    public void setupPieChart(PieChart pieChart, int userId) {
        List<PieEntry> entries = new ArrayList<>();

        // Calculer les dates du mois en arrière
        long currentTime = System.currentTimeMillis();
        long startOfPreviousMonth = Util.getStartOfPreviousMonth(currentTime);
        long endOfPreviousMonth = Util.getEndOfMonth(currentTime - (30L * 24 * 60 * 60 * 1000)); // Un mois en arrière

        // Requête SQL filtrant les données par utilisateur, mois spécifié et plage de dates
        Cursor cursor = db.query("SELECT Categorie.nom, SUM(UserTransaction.Montant) as total " +
                        "FROM UserTransaction " +
                        "JOIN Categorie ON UserTransaction.ID_Categorie = Categorie.id " +
                        "WHERE UserTransaction.ID_Utilisateur = ? AND " +
                        "UserTransaction.Date_transaction BETWEEN ? AND ? " + // Filtre par mois en arrière
                        "GROUP BY UserTransaction.ID_Categorie",
                new String[]{String.valueOf(userId), String.valueOf(startOfPreviousMonth), String.valueOf(endOfPreviousMonth)});

        while (cursor.moveToNext()) {
            String categorie = cursor.getString(0);
            float montant = cursor.getFloat(1);
            entries.add(new PieEntry(montant, categorie));
        }
        cursor.close();

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA});
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setUsePercentValues(true);
        pieChart.setCenterText("Dépenses");
        pieChart.setCenterTextSize(16f);

        pieChart.invalidate();
    }


    public void setupBarChart(BarChart barChart, int userId) {
        List<BarEntry> budgetEntries = new ArrayList<>();
        List<BarEntry> depenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Calculer les dates du mois en arrière
        long currentTime = System.currentTimeMillis();
        long startOfPreviousMonth = Util.getStartOfPreviousMonth(currentTime);
        long endOfPreviousMonth = Util.getEndOfMonth(currentTime - (30L * 24 * 60 * 60 * 1000)); // Un mois en arrière

        // Requête SQL pour filtrer les données par mois en arrière et par utilisateur
        String query = "SELECT c.nom, COALESCE(b.montant, 0) as budget, COALESCE(SUM(t.Montant), 0) as depense " +
                "FROM Categorie c " +
                "LEFT JOIN Budget b ON c.nom = b.categorie " +
                "LEFT JOIN UserTransaction t ON c.id = t.ID_Categorie " +
                "WHERE t.Date_transaction BETWEEN ? AND ? " + // Filtre par mois en arrière
                "AND t.ID_Utilisateur = ? " + // Filtre par utilisateur
                "GROUP BY c.id";

        Cursor cursor = db.query(query, new String[]{String.valueOf(startOfPreviousMonth), String.valueOf(endOfPreviousMonth), String.valueOf(userId)});

        int i = 0;
        while (cursor.moveToNext()) {
            String categorie = cursor.getString(0);
            float budget = cursor.getFloat(1);
            float depense = cursor.getFloat(2);

            labels.add(categorie);
            budgetEntries.add(new BarEntry(i, budget));
            depenseEntries.add(new BarEntry(i, depense));
            i++;
        }
        cursor.close();

        BarDataSet budgetDataSet = new BarDataSet(budgetEntries, "Budgets");
        budgetDataSet.setColor(Color.GREEN);
        budgetDataSet.setValueTextSize(12f);

        BarDataSet depenseDataSet = new BarDataSet(depenseEntries, "Dépenses");
        depenseDataSet.setColor(Color.RED);
        depenseDataSet.setValueTextSize(12f);

        BarData data = new BarData(budgetDataSet, depenseDataSet);
        data.setBarWidth(0.4f); // Ajuster la largeur des barres

        barChart.setData(data);
        barChart.getXAxis().setCenterAxisLabels(true);
        barChart.getXAxis().setGranularity(1f); // Espacement entre les groupes de barres
        barChart.groupBars(0f, 0.5f, 0.1f); // Groupement des barres

        barChart.invalidate();
    }

}




