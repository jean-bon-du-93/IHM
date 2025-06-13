# Expected User Interface Description for PokemonTCG Application

This document describes the expected user interface when `PokemonTCGIHM.java` is launched and a game is active, based on a review of the codebase.

## Overall Window:

*   The main application window will be titled **"PokemonTCG"**.
*   Its size will be dynamically set to 65% of the primary screen's width and height.
*   The content of the window is primarily the `VueDuJeu` component.
*   The main game view (`VueDuJeu`) has padding around its edges for better spacing (`game-view-root-padding` class).

## `VueDuJeu` Component (Main Game Area):

This component is a `VBox` (vertical layout) providing the main game interface. Its root VBox is styled with `game-view-root-padding` and has a spacing of 15px between its direct children.

*   **Top Element**:
    *   A `Label` (`instructionLabel`) displaying the current game instruction (e.g., "Choose a Pokémon to be active", "Player John's turn").
    *   Styled by `instruction-area`: This gives it a light gray background, padding, a bottom border, and an 18px font size, making it a distinct instruction bar.
*   **Player's View Area**:
    *   The `VueJoueurActif` component (`panneauDuJoueurActif`). This component is configured to grow vertically (`VBox.vgrow="ALWAYS"`), taking up a significant portion of the available space. It is displayed directly below the instruction label.
*   **Opponent's View Area**:
    *   The `VueAdversaire` component (`vueAdversaire`). This component is displayed below `VueJoueurActif` and is also configured to grow vertically (`VBox.vgrow="ALWAYS"`), sharing space with the player's view.

## `VueJoueurActif` Component (Active Player's View):

This component is a `VBox` with 10px internal spacing, styled with the `player-area` class (light blue background, padding, rounded border). Text elements generally use an 18px font.

*   **Elements (Top to Bottom):**
    1.  **Player Name (`nomDuJoueurLabel`)**: `Label` showing the active player's name, styled with `text-18px`.
    2.  **Active Pokémon (`pokemonActifButton`)**: `Button`, styled with `card-button` and `text-18px` (card-like appearance).
        *   Text: Name of the active Pokémon or "Aucun Pokémon actif".
        *   Action: Prints a debug message.
    3.  **Active Pokémon's Energy (`energiePokemonActifHBox`)**: `HBox` below the active Pokémon button, displaying `Label`s for attached energy, styled with `energy-tag`.
    4.  **Hand (`panneauMainHBox`)**: `HBox` containing `Button`s for hand cards, styled with `card-button` and `text-18px`.
        *   Action: Notifies game logic.
    5.  **Bench (`panneauBancHBox`)**: Centered `HBox` displaying 5 slots.
        *   **Occupied Slots**: `VBox` (styled `pokemon-node-display`) with Pokémon `Button` (styled `card-button text-18px`) and energy `Label`s (styled `energy-tag`).
        *   **Empty Slots**: Placeholder `Button` (styled `empty-bench-slot`). Action: Notifies game logic.
    6.  **Pass Button (`passerButton`)**: `Button` with text "Passer", styled with `text-18px`.

## `VueAdversaire` Component (Opponent's View):

This component is a `VBox` with 8px internal spacing, styled with the `opponent-area` class (light pink/lavender background, padding, rounded border). This is a read-only view.

*   **Elements (Top to Bottom):**
    1.  **Opponent Name (`nomAdversaireLabel`)**: `Label` showing the opponent's name, styled with `text-18px`. (Has a bottom margin).
    2.  **Static Label**: "Pokémon Actif de l'adversaire:", styled with `text-18px`.
    3.  **Opponent's Active Pokémon (`pokemonActifAdversaireDisplay`)**: `Label` styled with `opponent-card-display` and `text-18px` (card-like placeholder appearance), showing the name of the opponent's active Pokémon. (Has a minimum height).
    4.  **Opponent's Active Pokémon's Energy (`energiePokemonActifAdversaireHBox`)**: `HBox` displaying energy tags for the opponent's active Pokémon. (Has a bottom margin and minimum height).
    5.  **Static Label**: "Main de l'adversaire:", styled with `text-18px`.
    6.  **Opponent's Hand (`panneauMainAdversaireHBox`)**: An `HBox` with 3px spacing.
        *   Displays a series of `Label`s styled as card backs (`.opponent-card-back` class: gray background, border, fixed size) corresponding to the number of cards in the opponent's hand. (Has a bottom margin and minimum height).
    7.  **Static Label**: "Banc de l'adversaire:", styled with `text-18px`.
    8.  **Opponent's Bench (`panneauBancAdversaireHBox`)**: `HBox` (items aligned `CENTER_LEFT`, 5px spacing).
        *   Displays a `VBox` for each benched Pokémon (styled `pokemon-node-display`), which includes a `Label` for the Pokémon's name (styled `opponent-card-display text-18px`) and an `HBox` for its energy tags. (Has a bottom margin and minimum height).
    9.  **Counts Area (`HBox`)**: An `HBox` grouping the following labels, with padding above it:
        *   **Opponent's Deck Size (`deckAdversaireLabel`)**: `Label` (e.g., "Deck Adv.: 30"), styled with `text-18px`.
        *   **Opponent's Discard Pile Size (`defausseAdversaireLabel`)**: `Label` (e.g., "Défausse Adv.: 10"), styled with `text-18px`.
        *   **Opponent's Prize Cards (`prixAdversaireLabel`)**: `Label` (e.g., "Prix Adv.: 4"), styled with `text-18px`.
*   All displays update dynamically.

## Styling (`style.css`):

This section summarizes the key styles and their effects.

*   **General**:
    *   Font "Verdana" is the base family.
    *   `.root` style: Default text fill black, base font size 12px.
    *   `.game-view-root-padding`: Adds padding around the main game area.
*   **Text & Buttons**:
    *   `.label.text-18px`, `.button.text-18px`: For 18px font size.
*   **Specific Areas**:
    *   `.instruction-area`: For the instruction bar (light gray background, padding, border, 18px font).
    *   `.player-area`: For the active player's section (light blue background, padding, rounded border).
    *   `.opponent-area`: For the opponent's section (light pink/lavender background, padding, rounded border).
*   **Card-like Elements**:
    *   `.card-button`: For player's hand cards and active Pokémon button (fixed size, border, white background).
    *   `.pokemon-node-display`: For VBox containers of benched Pokémon (fixed width, border, white background).
    *   `.empty-bench-slot`: For empty bench slot placeholders (dashed border, specific background, smaller font).
    *   `.opponent-card-display`: For labels showing opponent's Pokémon (active/bench) as card placeholders.
    *   `.opponent-card-back`: For labels representing backs of cards in opponent's hand (gray background, border, fixed size).
*   **Other UI Elements**:
    *   `.energy-tag`: For energy labels (smaller font, padding, border, light gray background).
    *   `.bordered-titled-title`, etc.: For titled panes.
```
