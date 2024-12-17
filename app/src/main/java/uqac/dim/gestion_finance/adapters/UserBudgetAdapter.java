package uqac.dim.gestion_finance.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executors;

import uqac.dim.gestion_finance.BudgetActivity;
import uqac.dim.gestion_finance.EditerBudgetActivity;
import uqac.dim.gestion_finance.R;
import uqac.dim.gestion_finance.database.AppDatabase;
import uqac.dim.gestion_finance.entities.Budget;

public class UserBudgetAdapter extends RecyclerView.Adapter<UserBudgetAdapter.BudgetViewHolder> {

    public interface OnLastBudgetRemovedListener {
        void onLastBudgetRemoved();
    }

    private final Context context;
    private final AppDatabase db;
    private List<Budget> budgets;
    private final OnLastBudgetRemovedListener onLastBudgetRemovedListener;
    private String currency; // Devise actuelle (modifiable)

    // Constructeur pour initialiser le contexte, la liste des budgets, le callback et la devise
    public UserBudgetAdapter(Context context, List<Budget> budgets, OnLastBudgetRemovedListener onLastBudgetRemovedListener, String currency) {
        this.context = context;
        this.budgets = budgets;
        this.db = AppDatabase.getDatabase(context);
        this.onLastBudgetRemovedListener = onLastBudgetRemovedListener;
        this.currency = currency != null ? currency : "EUR"; // Défaut à "EUR" si aucune devise fournie
    }

    // Met à jour la liste des budgets dans l'adaptateur
    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
        notifyDataSetChanged();
    }

    // Met à jour la devise utilisée pour afficher les montants
    public void setCurrency(String currency) {
        this.currency = currency != null ? currency : context.getString(R.string.default_currency);
        notifyDataSetChanged(); // Rafraîchir l'affichage
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate le layout de l'élément de la liste
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        // Récupère l'objet Budget actuel
        Budget budget = budgets.get(position);

        // Remplit les TextViews avec les données du budget
        holder.textViewName.setText(budget.nom);

        // Formater le montant avec la devise actuelle
        holder.textViewAmount.setText(String.format("%.2f %s", budget.montant, currency));

        // Vérifie la récurrence et ajuste l'affichage
        switch (budget.recurrence) {
            case Budget.RECURRENCE_NONE:
                holder.textViewRecurrence.setVisibility(View.GONE);
                holder.flexibleSpace.setVisibility(View.VISIBLE); // Afficher un espace flexible si la récurrence est absente
                break;
            case Budget.RECURRENCE_MONTHLY:
                holder.textViewRecurrence.setVisibility(View.VISIBLE);
                holder.flexibleSpace.setVisibility(View.GONE);
                holder.textViewRecurrence.setText(context.getString(R.string.budget_recurrence_monthly));
                break;
            case Budget.RECURRENCE_YEARLY:
                holder.textViewRecurrence.setVisibility(View.VISIBLE);
                holder.flexibleSpace.setVisibility(View.GONE);
                holder.textViewRecurrence.setText(context.getString(R.string.budget_recurrence_yearly));
                break;
            default:
                holder.textViewRecurrence.setVisibility(View.VISIBLE);
                holder.flexibleSpace.setVisibility(View.GONE);
                holder.textViewRecurrence.setText(budget.recurrence); // Affiche la récurrence brute si inconnue
        }

        // Configure l'apparence en fonction de l'état actif/inactif
        updateViewAppearance(holder, budget.actif);

        // Gestion du bouton Activer/Désactiver
        holder.buttonToggleActive.setOnClickListener(v -> {
            budget.actif = !budget.actif; // Inverse l'état actif/inactif
            Executors.newSingleThreadExecutor().execute(() -> {
                db.budgetDao().update(budget); // Met à jour la base de données
                ((Activity) context).runOnUiThread(() -> {
                    // Met à jour uniquement cette vue
                    updateViewAppearance(holder, budget.actif);
                });
            });
        });

        // Gestion du bouton Éditer
        holder.buttonEditBudget.setOnClickListener(v -> {
            Log.d("UserBudgetAdapter", "Éditer le budget : " + budget.nom);
            Intent intent = new Intent(context, EditerBudgetActivity.class);
            intent.putExtra("budgetId", budget.id); // Passez l'ID du budget
            context.startActivity(intent);
        });

        // Gestion du bouton Supprimer
        holder.buttonDeleteBudget.setOnClickListener(v -> {
            if (context instanceof BudgetActivity) {
                ((BudgetActivity) context).attemptDeleteBudget(budget.id, budget.nom);            }
        });
    }

    // Supprime un budget à une position donnée
    private void removeBudgetAtPosition(int position) {
        budgets.remove(position); // Retire le budget de la liste
        notifyItemRemoved(position); // Animation fluide pour la suppression
        notifyItemRangeChanged(position, budgets.size()); // Met à jour les indices suivants

        // Vérifie si la liste est maintenant vide
        if (budgets.isEmpty() && onLastBudgetRemovedListener != null) {
            onLastBudgetRemovedListener.onLastBudgetRemoved();
        }
    }

    @Override
    public int getItemCount() {
        // Retourne le nombre de budgets dans la liste
        return (budgets != null) ? budgets.size() : 0;
    }

    /**
     * Met à jour l'apparence visuelle de l'élément en fonction de l'état actif/inactif.
     */
    private void updateViewAppearance(BudgetViewHolder holder, boolean isActive) {
        if (isActive) {
            holder.itemView.setAlpha(1f); // Vue pleinement visible
            holder.buttonToggleActive.setImageResource(R.drawable.ic_active); // Icône "Actif"
            holder.buttonToggleActive.setColorFilter(context.getResources().getColor(R.color.green));
        } else {
            holder.itemView.setAlpha(0.5f); // Vue grisée
            holder.buttonToggleActive.setImageResource(R.drawable.ic_inactive); // Icône "Inactif"
            holder.buttonToggleActive.setColorFilter(context.getResources().getColor(R.color.gray));
        }
    }

    // ViewHolder pour lier les vues de chaque élément
    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewAmount, textViewRecurrence;
        ImageButton buttonEditBudget, buttonDeleteBudget, buttonToggleActive;
        View flexibleSpace;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialisation des vues
            textViewName = itemView.findViewById(R.id.textViewBudgetName);
            textViewAmount = itemView.findViewById(R.id.textViewBudgetAmount);
            textViewRecurrence = itemView.findViewById(R.id.textViewBudgetRecurrence);
            buttonEditBudget = itemView.findViewById(R.id.buttonEditBudget);
            buttonDeleteBudget = itemView.findViewById(R.id.buttonDeleteBudget);
            buttonToggleActive = itemView.findViewById(R.id.buttonToggleActive);
            flexibleSpace = itemView.findViewById(R.id.flexibleSpace); // Vue pour gérer l'espace flexible
        }
    }
}