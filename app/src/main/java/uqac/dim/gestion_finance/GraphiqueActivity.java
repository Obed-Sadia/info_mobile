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

    // Configuration du graphique circulaire (PieChart)
    public void setupPieChart(PieChart pieChart) {
        List<PieEntry> entries = new ArrayList<>();

        Cursor cursor = db.query("SELECT Categorie.nom, SUM(UserTransaction.Montant) as total FROM UserTransaction JOIN Categorie ON UserTransaction.ID_Categorie = Categorie.id GROUP BY UserTransaction.ID_Categorie", new String[]{});
        while (cursor.moveToNext()) {
            String categorie = cursor.getString(0);
            float montant = cursor.getFloat(1);
            entries.add(new PieEntry(montant, categorie));
        }
        cursor.close();

        PieDataSet dataSet = new PieDataSet(entries,"");
        dataSet.setColors(new int[]{Color.MAGENTA, Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED});
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setUsePercentValues(true);
        pieChart.setCenterText("Dépenses");
        pieChart.setCenterTextSize(16f);


        pieChart.invalidate();
    }




}




