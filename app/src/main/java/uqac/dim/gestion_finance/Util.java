package uqac.dim.gestion_finance;

import java.util.Calendar;

public class Util {

    /**
     * Retourne le timestamp du premier jour du mois précédent à partir de la date courante.
     *
     * @param currentTime Le timestamp actuel.
     * @return Le timestamp du premier jour du mois précédent.
     */
    public static long getStartOfPreviousMonth(long currentTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTimeInMillis();
    }

    /**
     * Retourne le timestamp du dernier jour du mois précédent à partir de la date courante.
     *
     * @param currentTime Le timestamp actuel.
     * @return Le timestamp du dernier jour du mois précédent.
     */
    public static long getEndOfMonth(long currentTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTimeInMillis();
    }
}
