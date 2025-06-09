# Expected User Interface Description for PokemonTCG Application

This document describes the expected user interface when `PokemonTCGIHM.java` is launched and a game is active, based on a review of the codebase.

## Overall Window:

*   The main application window will be titled **"PokemonTCG"**.
*   Its size will be dynamically set to 65% of the primary screen's width and height.
*   The content of the window is primarily the `VueDuJeu` component.
*   The main game view (`VueDuJeu`) has padding around its edges for better spacing.

## `VueDuJeu` Component (Main Game Area):

This component is a `VBox` (vertical layout) providing the main game interface. It is styled with the `game-view-root-padding` class.

*   **Top Element**:
    *   A `Label` (`instructionLabel`) displaying the current game instruction (e.g., "Choose a Pokémon to be active", "Player John's turn").
    *   Styled by `instruction-area`: This gives it a light gray background, padding, a bottom border, and an 18px font size, making it a distinct instruction bar.
*   **Middle Element**:
    *   An `HBox` (horizontal layout) containing two main sections, side-by-side, with 20px spacing between them:
        *   **Left Side**: The `VueJoueurActif` component (active player's view).
        *   **Right Side**: The `VueAdversaire` component (opponent's view).

## `VueJoueurActif` Component (Active Player's View - Left Side):

This component is a `VBox` with 10px internal spacing, styled with the `player-area` class, giving it a light blue background, padding, and a rounded border. Text elements generally use an 18px font.

*   **Elements (Top to Bottom):**
    1.  **Player Name (`nomDuJoueurLabel`)**: `Label` showing the active player's name (e.g., "John"), styled with `text-18px`.
    2.  **Active Pokémon (`pokemonActifButton`)**: `Button`.
        *   Styled with `card-button` and `text-18px`: Appears card-like with a border, white background, fixed size, and 18px font.
        *   Text: Name of the active Pokémon (e.g., "Charizard") or "Aucun Pokémon actif".
        *   Action: Prints a debug message to console.
    3.  **Active Pokémon's Energy (`energiePokemonActifHBox`)**: `HBox` below the active Pokémon button (spacing 2px, alignment `CENTER_LEFT`).
        *   Displays `Label`s for each energy type and count, styled with `energy-tag`: chip-like appearance with a light gray background, border, and rounded corners.
        *   Dynamically updates with energy changes.
    4.  **Hand (`panneauMainHBox`)**: `HBox` with 5px spacing.
        *   Contains `Button`s for each card in the player's hand.
        *   Hand card buttons are styled with `card-button` and `text-18px` (border, white background, fixed size, 18px font).
        *   Button text: Card name.
        *   Action: Notifies game logic (`jeu.uneCarteDeLaMainAEteChoisie(carte.getId())`).
        *   Dynamically updates with hand changes.
    5.  **Bench (`panneauBancHBox`)**: Centered `HBox` with 5px spacing.
        *   Always displays 5 slots.
        *   **Occupied Slots**: Display a `VBox` for each benched Pokémon, styled with `pokemon-node-display` (card-like with border, white background, fixed width). This `VBox` contains:
            *   A `Button` with the benched Pokémon's name, styled with `card-button` and `text-18px`. Clicking it prints a debug message.
            *   An `HBox` below the button, displaying `Label`s for attached energy, styled with `energy-tag`.
        *   **Empty Slots**: Display a `Button` styled with `empty-bench-slot`: placeholder look with a light gray dashed border, specific background, smaller text, and fixed size.
            *   Action: Clicking an empty slot button notifies the game logic (`jeu.unEmplacementVideDuBancAEteChoisi(String.valueOf(slotIndex))`).
        *   The entire bench area updates when Pokémon are added to or removed from the bench (triggering a full reconstruction of the 5 slots).
    6.  **Pass Button (`passerButton`)**: `Button` with text "Passer", styled with `text-18px`.
        *   Action: Notifies game logic (`jeu.passerAEteChoisi()`) and prints a debug message.

## `VueAdversaire` Component (Opponent's View - Right Side):

This component is a `VBox` with 10px internal spacing, styled with the `opponent-area` class, giving it a light pink/lavender background, padding, and a rounded border. Text elements generally use an 18px font. This is a read-only view.

*   **Elements (Top to Bottom):**
    1.  **Opponent Name (`nomAdversaireLabel`)**: `Label` showing the opponent's name, styled with `text-18px`.
    2.  **Static Label**: "Pokémon Actif:", styled with `text-18px`.
    3.  **Opponent's Active Pokémon (`pokemonActifAdversaireLabel`)**: `Label` styled with `opponent-card-display` and `text-18px` (card-like placeholder appearance), showing the name of the opponent's active Pokémon.
    4.  **Static Label**: "Banc:", styled with `text-18px`.
    5.  **Opponent's Bench (`bancAdversaireHBox`)**: `HBox` (items aligned `CENTER_LEFT`, 5px spacing).
        *   Displays `Label`s for each benched Pokémon, styled with `opponent-card-display` and `text-18px` (card-like placeholder appearance).
    6.  **Opponent's Hand Size (`mainAdversaireLabel`)**: `Label` (e.g., "Main Adv.: 5"), styled with `text-18px`.
    7.  **Opponent's Deck Size (`deckAdversaireLabel`)**: `Label` (e.g., "Deck Adv.: 30"), styled with `text-18px`.
    8.  **Opponent's Discard Pile Size (`defausseAdversaireLabel`)**: `Label` (e.g., "Défausse Adv.: 10"), styled with `text-18px`.
    9.  **Opponent's Prize Cards (`prixAdversaireLabel`)**: `Label` (e.g., "Prix Adv.: 4"), styled with `text-18px`.
*   All displays update dynamically with opponent's state changes or when the active player (and thus opponent) changes.

## Styling (`style.css`):

This section summarizes the key styles and their effects.

*   **General**:
    *   Font "Verdana" is the base family (specified in `.root`).
    *   `.root` style also defines default text fill black and base font size of 12px.
    *   `.game-view-root-padding`: Adds overall padding around the main game area.
*   **Text & Buttons**:
    *   `.label.text-18px`, `.button.text-18px`: Applied to most primary labels and buttons for an 18px font size.
*   **Specific Areas**:
    *   `.instruction-area`: Light gray background, padding, bottom border, 18px font for the instruction bar.
    *   `.player-area`: Light blue background, padding, rounded border for the active player's section.
    *   `.opponent-area`: Light pink/lavender background, padding, rounded border for the opponent's section.
*   **Card-like Elements**:
    *   `.card-button`: Styles hand card buttons and the active Pokémon button with a fixed size, border, white background, and top-center text alignment.
    *   `.pokemon-node-display`: Styles the VBox container for benched Pokémon with a fixed width, border, and white background.
    *   `.empty-bench-slot`: Styles empty bench slots with a dashed border, specific background, and smaller font.
    *   `.opponent-card-display`: Styles labels for opponent's Pokémon (active/bench) with a border, white background, and padding for a placeholder card look.
*   **Other UI Elements**:
    *   `.energy-tag`: Styles energy labels with a smaller font, padding, border, and light gray background for a chip-like look.
    *   `.bordered-titled-title`, `.bordered-titled-border`, `.bordered-titled-content`: Styles for creating titled panes (likely used for other UI parts not detailed here, or general purpose).

```
