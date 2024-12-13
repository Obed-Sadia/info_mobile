package uqac.dim.gestion_finance.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

import uqac.dim.gestion_finance.R;
import uqac.dim.gestion_finance.entities.UserTransaction;

public class UserTransactionAdapter extends RecyclerView.Adapter<UserTransactionAdapter.ViewHolder> {

    private static final String TAG = "UserTransactionAdapter";

    private List<UserTransaction> transactions;
    private String currentCurrency;
    private final Context context;

    public UserTransactionAdapter(Context context, List<UserTransaction> transactions) {
        this.context = context;
        this.transactions = transactions;
        updateCurrency(); // Initialiser la devise
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (transactions == null || transactions.isEmpty()) {
            Log.w(TAG, "onBindViewHolder: Transactions list is empty");
            return;
        }

        UserTransaction transaction = transactions.get(position);

        // Nom de la transaction
        holder.transactionName.setText(transaction.Nom_transaction);

        // Formatage du montant (deux décimales avec devise)
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        holder.transactionAmount.setText(String.format("%s %s", decimalFormat.format(transaction.Montant), currentCurrency));

        // Date de la transaction
        holder.transactionDate.setText(transaction.Date_transaction);
    }

    @Override
    public int getItemCount() {
        return (transactions != null) ? transactions.size() : 0;
    }

    /**
     * Met à jour la liste des transactions et rafraîchit l'affichage.
     */
    public void setTransactions(List<UserTransaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    /**
     * Met à jour la devise utilisée pour les montants.
     */
    public void updateCurrency() {
        SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        currentCurrency = prefs.getString("CURRENCY", "EUR"); // Valeur par défaut : "EUR"
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView transactionName;
        final TextView transactionAmount;
        final TextView transactionDate;

        ViewHolder(View itemView) {
            super(itemView);
            transactionName = itemView.findViewById(R.id.transactionName);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDate = itemView.findViewById(R.id.transactionDate);
        }
    }
}