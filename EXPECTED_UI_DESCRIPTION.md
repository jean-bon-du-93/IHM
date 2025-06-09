# Expected User Interface Description for PokemonTCG Application

This document describes the expected user interface when `PokemonTCGIHM.java` is launched and a game is active, based on a review of the codebase.

## Overall Window:

*   The main application window will be titled **"PokemonTCG"**.
*   Its size will be dynamically set to 65% of the primary screen's width and height.
*   The content of the window is primarily the `VueDuJeu` component.

## `VueDuJeu` Component (Main Game Area):

This component is a `VBox` (vertical layout) providing the main game interface.

*   **Top Element**:
    *   A `Label` (`instructionLabel`) displaying the current game instruction (e.g., "Choose a Pokémon to be active", "Player John's turn").
    *   Font size: 18px.
*   **Middle Element**:
    *   An `HBox` (horizontal layout) containing two main sections, side-by-side, with 20px spacing between them:
        *   **Left Side**: The `VueJoueurActif` component (active player's view).
        *   **Right Side**: The `VueAdversaire` component (opponent's view).

## `VueJoueurActif` Component (Active Player's View - Left Side):

This component is a `VBox` with 10px internal spacing. Text elements generally use an 18px font.

*   **Elements (Top to Bottom):**
    1.  **Player Name (`nomDuJoueurLabel`)**: `Label` showing the active player's name (e.g., "John").
    2.  **Active Pokémon (`pokemonActifButton`)**: `Button`.
        *   Text: Name of the active Pokémon (e.g., "Charizard") or "Aucun Pokémon actif".
        *   Action: Prints a debug message to console ("pokemonActifButton clicked... Action to be defined.").
    3.  **Active Pokémon's Energy (`energiePokemonActifHBox`)**: `HBox` below the active Pokémon button.
        *   Displays `Label`s for each energy type and count attached to the active Pokémon (e.g., "FIRE x2").
        *   Energy labels: 10px font, padding, light gray border.
        *   Dynamically updates with energy changes.
    4.  **Hand (`panneauMainHBox`)**: `HBox` with 5px spacing.
        *   Contains `Button`s for each card in the player's hand.
        *   Button text: Card name.
        *   Action: Notifies game logic (`jeu.uneCarteDeLaMainAEteChoisie(carte.getId())`).
        *   Dynamically updates with hand changes.
    5.  **Bench (`panneauBancHBox`)**: Centered `HBox` with 5px spacing.
        *   Contains a `VBox` for each benched Pokémon. Each `VBox` includes:
            *   A `Button` with the benched Pokémon's name. Action: Prints debug message ("Bouton Pokémon du banc cliqué... Action à définir.").
            *   An `HBox` below the button displaying `Label`s for attached energy (similar to active Pokémon's energy, but 9px font).
        *   Updates with bench changes; energy display refreshes when bench refreshes.
    6.  **Pass Button (`passerButton`)**: `Button` with text "Passer".
        *   Action: Notifies game logic (`jeu.passerAEteChoisi()`) and prints a debug message.

## `VueAdversaire` Component (Opponent's View - Right Side):

This component is a `VBox` with 10px internal spacing. Text elements generally use an 18px font. This is a read-only view.

*   **Elements (Top to Bottom):**
    1.  **Opponent Name (`nomAdversaireLabel`)**: `Label` showing the opponent's name (e.g., "Paul").
    2.  **Static Label**: "Pokémon Actif:"
    3.  **Opponent's Active Pokémon (`pokemonActifAdversaireLabel`)**: `Label` showing the name of the opponent's active Pokémon (or "Aucun" / "N/A").
    4.  **Static Label**: "Banc:"
    5.  **Opponent's Bench (`bancAdversaireHBox`)**: `HBox` (items aligned `CENTER_LEFT`, 5px spacing).
        *   Displays simple `Label`s for each benched Pokémon, showing their names. Labels have a border and padding.
    6.  **Opponent's Hand Size (`mainAdversaireLabel`)**: `Label` (e.g., "Main Adv.: 5").
    7.  **Opponent's Deck Size (`deckAdversaireLabel`)**: `Label` (e.g., "Deck Adv.: 30").
    8.  **Opponent's Discard Pile Size (`defausseAdversaireLabel`)**: `Label` (e.g., "Défausse Adv.: 10").
    9.  **Opponent's Prize Cards (`prixAdversaireLabel`)**: `Label` (e.g., "Prix Adv.: 4").
*   All displays update dynamically with opponent's state changes or when the active player (and thus opponent) changes.

## Styling (`style.css`):

*   Font "IM Fell English SC" is imported.
*   `.root` style: Default text fill black, font size 12px.
*   `.text-18px` class: Applied to most labels and buttons for an 18px font size.
*   Energy card labels: Smaller font sizes (10px active, 9px bench), padding, border.
*   Opponent's bench Pokémon labels: Border and padding.
