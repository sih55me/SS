package cakar.search;

import android.content.SearchRecentSuggestionsProvider;

public class SearchProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "cakar.search.HistorySearchProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}