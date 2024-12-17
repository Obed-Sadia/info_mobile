package uqac.dim.gestion_finance.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.Executors;

import uqac.dim.gestion_finance.R;
import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.UserTransaction;

public class UserTransactionAdapter extends RecyclerView.Adapter<UserTransactionAdapter.ViewHolder> {

    private List<UserTransaction> transactions;
    private String currentCurrency;
    private final Context context;
    private final OnTransactionActionListener listener; // Listener pour les actions
    private final OnLastTransactionRemovedListener onLastTransactionRemovedListener;

    // Interface pour gérer les actions d'édition et suppression
    public interface OnTransactionActionListener {
        void onEditTransaction(UserTransaction transaction);
        void onDeleteTransaction(UserTransaction transaction);
    }

    // Interface pour signaler qu'il n'y a plus de transactions
    public interface OnLastTransactionRemovedListener {
        void onLastTransactionRemoved();
    }

    // Constructeur mis à jour
    public UserTransactionAdapter(Context context, List<UserTransaction> transactions,
                                  OnTransactionActionListener listener,
                                  OnLastTransactionRemovedListener lastTransactionListener) {
        this.context = context;
        this.transactions = transactions;
        this.listener = listener;
        this.onLastTransactionRemovedListener = lastTransactionListener;
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
        holder.buttonDeleteTransaction.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.confirm_delete_transaction, transaction.Nom_transaction))
                    .setMessage(R.string.delete_transaction_confirmation)
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        // Suppression confirmée
                        Executors.newSingleThreadExecutor().execute(() -> {
                            AppDatabase db = AppDatabase.getDatabase(context);
                            db.transactionDao().deleteTransactionById(transaction.ID_Transaction);
                            ((Activity) context).runOnUiThread(() -> {
                                transactions.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, transactions.size());

                                // Vérification si la liste est vide après suppression
                                if (transactions.isEmpty() && onLastTransactionRemovedListener != null) {
                                    onLastTransactionRemovedListener.onLastTransactionRemoved();
                                }
                            });
                        });
                    })
                    .setNegativeButton(R.string.cancel, null) // Annuler
                    .show();
        });
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