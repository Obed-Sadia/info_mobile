package uqac.dim.gestion_finance.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import uqac.dim.gestion_finance.R;
import uqac.dim.gestion_finance.entities.UserTransaction;

public class UserTransactionAdapter extends RecyclerView.Adapter<UserTransactionAdapter.ViewHolder> {

    private List<UserTransaction> transactions;
    private String currentCurrency;
    private Context context;

    public UserTransactionAdapter(Context context, List<UserTransaction> transactions) {
        this.context = context;
        this.transactions = transactions;
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
        holder.transactionName.setText(transaction.Nom_transaction);
        holder.transactionAmount.setText(String.format("%.2f %s", transaction.Montant, currentCurrency));
        holder.transactionDate.setText(transaction.Date_transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateCurrency() {
        SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        currentCurrency = prefs.getString("CURRENCY", "EUR"); // EUR par défaut
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView transactionName;
        TextView transactionAmount;
        TextView transactionDate;

        ViewHolder(View itemView) {
            super(itemView);
            transactionName = itemView.findViewById(R.id.transactionName);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDate = itemView.findViewById(R.id.transactionDate);
        }
    }
}