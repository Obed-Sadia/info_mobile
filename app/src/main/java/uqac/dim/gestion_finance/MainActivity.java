package uqac.dim.gestion_finance;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BubbleChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import uqac.dim.gestion_finance.dao.BudgetDao;
import uqac.dim.gestion_finance.dao.UserTransactionDao;
import uqac.dim.gestion_finance.dao.UtilisateurDao;
import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.Budget;
import uqac.dim.gestion_finance.entities.Utilisateur;
import uqac.dim.gestion_finance.Util;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String CHANNEL_ID = "ALERT_CHANNEL_ID";
    private static int NOTIFICATION_ID = 1000;

    private TextView welcomeMessage;
    private TextView alertMessage;
    private BottomNavigationView bottomNavigation;

    private AppDatabase db;
    private UserTransactionDao transactionDao;
    private BudgetDao budgetDao;
    private UtilisateurDao utilisateurDao;

    private BroadcastReceiver settingsReceiver;
    private static final int REQUEST_POST_NOTIFICATIONS = 1;

    private GraphiqueActivity graphiqueActivity;
    private PieChart pieChart;
    private BarChart barChart;
    private RadarChart radarChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);
        Log.d(TAG, "onCreate: MainActivity started");

        createNotificationChannel();
        requestNotificationPermission();
        initializeViews();
        initializeDatabase();
        loadUserData();
        loadRecentAlerts();
        setupNavigation();
        initializeSettingsReceiver();

        // Initialiser GraphiqueActivity avec la base de données
        graphiqueActivity = new GraphiqueActivity(db.getOpenHelper().getWritableDatabase());

        // Initialiser et configurer les graphiques
        PieChart pieChart = findViewById(R.id.pieChartExpenses);


        // Récupérer l'identifiant de l'utilisateur
        int userId = getCurrentUserId(); // Fonction déjà définie

        // Passer l'identifiant de l'utilisateur aux méthodes de configuration du graphique
        graphiqueActivity.setupPieChart(pieChart);


    }
    private int getLastUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getInt("USER_ID", -1);
    }


    private void initializeViews() {
        welcomeMessage = findViewById(R.id.welcomeMessage);
        alertMessage = findViewById(R.id.alertMessage);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void initializeDatabase() {
        db = AppDatabase.getDatabase(getApplicationContext());
        transactionDao = db.transactionDao();
        budgetDao = db.budgetDao();
        utilisateurDao = db.utilisateurDao();
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.navigation_transaction) {
                startActivity(new Intent(this, TransactionActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_budget) {
                startActivity(new Intent(this, BudgetActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_parametres) {
                startActivity(new Intent(this, ParametresActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void initializeSettingsReceiver() {
        settingsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("SETTINGS_UPDATED".equals(intent.getAction())) {
                    updateUI();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("SETTINGS_UPDATED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(settingsReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                registerReceiver(settingsReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            }
        }
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(settingsReceiver);
    }

    private void updateUI() {
        loadUserData();
        loadRecentAlerts();
    }

    private void loadUserData() {
        int userId = getCurrentUserId();
        if (userId == -1) {
            Log.e(TAG, "Invalid user ID");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            Utilisateur user = utilisateurDao.getById(userId);
            runOnUiThread(() -> {
                if (user != null) {
                    welcomeMessage.setText(getString(R.string.welcome_user, user.Nom));
                }
            });
        });
    }

    private void loadRecentAlerts() {
        Executors.newSingleThreadExecutor().execute(() -> {
            StringBuilder alerts = new StringBuilder();

            GlobalSettings globalSettings = new GlobalSettings(this);
            String currency = globalSettings.getCurrency();

            // Charger depuis SharedPreferences les alertes déjà envoyées
            SharedPreferences prefs = getSharedPreferences("AlertsPrefs", MODE_PRIVATE);
            Set<String> sentAlerts = prefs.getStringSet("SENT_ALERTS", new HashSet<>());

            // Charger les budgets
            List<Budget> budgets = budgetDao.getAllBudgets();
            budgets.sort((b1, b2) -> {
                Double totalSpent1 = transactionDao.getTotalAmountByBudgetId(b1.id);
                Double totalSpent2 = transactionDao.getTotalAmountByBudgetId(b2.id);

                double spentAmount1 = (totalSpent1 != null) ? totalSpent1 : 0.0;
                double spentAmount2 = (totalSpent2 != null) ? totalSpent2 : 0.0;

                double percentage1 = (spentAmount1 / b1.montant) * 100;
                double percentage2 = (spentAmount2 / b2.montant) * 100;

                return Double.compare(percentage2, percentage1);
            });

            Set<String> newSentAlerts = new HashSet<>(sentAlerts); // Copie existante

            for (Budget budget : budgets) {
                Double totalSpent = transactionDao.getTotalAmountByBudgetId(budget.id);
                double spentAmount = (totalSpent != null) ? totalSpent : 0.0;
                double percentage = (spentAmount / budget.montant) * 100;

                String alertString = null;
                String alertKey = null; // clé unique pour identifier l'alerte (ex: "budgetId-pourcentage")

                if (percentage > 100) {
                    double overSpent = spentAmount - budget.montant;
                    double overPercentage = percentage - 100;
                    alertString = getString(R.string.alert_budget_exceeded, budget.nom, overSpent, overPercentage, currency);
                    alertKey = budget.id + "-EXCEEDED";
                } else if (percentage >= 100) {
                    alertString = getString(R.string.alert_budget_100, budget.nom, spentAmount, currency);
                    alertKey = budget.id + "-100";
                } else if (percentage >= 75) {
                    alertString = getString(R.string.alert_budget_75, budget.nom, spentAmount, currency);
                    alertKey = budget.id + "-75";
                } else if (percentage >= 50) {
                    alertString = getString(R.string.alert_budget_50, budget.nom, spentAmount, currency);
                    alertKey = budget.id + "-50";
                } else if (percentage >= 25) {
                    alertString = getString(R.string.alert_budget_25, budget.nom, spentAmount, currency);
                    alertKey = budget.id + "-25";
                }

                if (alertString != null && alertKey != null) {
                    // Vérifier si cette alerte a déjà été envoyée
                    if (!sentAlerts.contains(alertKey)) {
                        // Nouvelle alerte, on l'ajoute et on envoie la notif
                        alerts.append(alertString).append("\n");
                        sendNotification(alertString);
                        newSentAlerts.add(alertKey);
                    } else {
                        // Alerte déjà envoyée, on ne notifie pas à nouveau
                    }
                }
            }

            // Sauvegarder la liste des alertes envoyées
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet("SENT_ALERTS", newSentAlerts);
            editor.apply();

            String finalAlerts = alerts.toString().trim();
            runOnUiThread(() -> alertMessage.setText(finalAlerts.isEmpty() ? getString(R.string.no_alerts) : finalAlerts));
        });
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getInt("USER_ID", -1);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        GlobalSettings globalSettings = new GlobalSettings(newBase);
        String language = globalSettings.getLanguage();
        Context context = LocaleHelper.wrap(newBase, language);
        super.attachBaseContext(context);
    }

    private void createNotificationChannel() {
        // Notification channels are required for Android O (API 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alert Notifications";
            String description = "Notifications for budget alerts";
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            android.app.NotificationChannel channel = new android.app.NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
            }
        }
    }

    private void sendNotification(String message) {
        // Check again if we have permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, do not send notification
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(getString(R.string.section_alerts)) // "Alerts"
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID++, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
