package uqac.dim.gestion_finance.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

import uqac.dim.gestion_finance.R;
import uqac.dim.gestion_finance.entities.UserTransaction;

public class UserTransactionAdapter extends RecyclerView.Adapter<UserTransactionAdapter.ViewHolder> {

    private List<UserTransaction> transactions;
    private String currentCurrency;
    private final Context context;
    private final OnTransactionActionListener listener; // Listener pour les actions

    // Interface pour gérer les actions d'édition et suppression
    public interface OnTransactionActionListener {
        void onEditTransaction(UserTransaction transaction);
        void onDeleteTransaction(UserTransaction transaction);
    }

    // Constructeur mis à jour
    public UserTransactionAdapter(Context context, List<UserTransaction> transactions, OnTransactionActionListener listener) {
        this.context = context;
        this.transactions = transactions;
        this.listener = listener;
        updateCurrency();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserTransaction transaction = transactions.get(position);

        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        // Affichage des données
        holder.transactionName.setText(transaction.Nom_transaction);
        holder.transactionAmount.setText(String.format("%s %s", decimalFormat.format(transaction.Montant), currentCurrency));
        holder.transactionDate.setText(transaction.Date_transaction);

        // Écouteurs pour les boutons d'action
        holder.buttonEditTransaction.setOnClickListener(v -> listener.onEditTransaction(transaction));
        holder.buttonDeleteTransaction.setOnClickListener(v -> listener.onDeleteTransaction(transaction));
    }

    @Override
    public int getItemCount() {
        return (transactions != null) ? transactions.size() : 0;
    }

    public void setTransactions(List<UserTransaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    public void updateCurrency() {
        SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        currentCurrency = prefs.getString("CURRENCY", "EUR");
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView transactionName;
        final TextView transactionAmount;
        final TextView transactionDate;
        final ImageButton buttonEditTransaction;
        final ImageButton buttonDeleteTransaction;

        ViewHolder(View itemView) {
            super(itemView);
            transactionName = itemView.findViewById(R.id.transactionName);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            buttonEditTransaction = itemView.findViewById(R.id.buttonEditTransaction);
            buttonDeleteTransaction = itemView.findViewById(R.id.buttonDeleteTransaction);
        }
    }
}