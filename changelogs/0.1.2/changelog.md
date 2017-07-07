**General**
- Mostly bug fixes and internal changes. React is fairly stable now, and is therefore being moved to beta.

**Actions**
- The "kill" and "damage" actions now accept an optional "damage_source" parameter, which simulates a specific type of damage. This affects, among other things, the death message (if the mob dies).
- Added a "feed" action, which adds the specified amount to the player's food bar.
- Added a "set_position" action, which set's the position of the mob. This accepts relative positions, for example "~10" translates to mob position + 10, whereas "~-10" translates to mob position - 10, and "~" translates to mob position. An example may be found [here](https://gist.github.com/coolsquid/3baff56b7d1a2e9af2981059e2ca90d5).

**Conditions**
- Fixed "command_arguments". The condition now accepts a regex string, which is matched with the entire argument set. An example may be found [here](https://gist.github.com/coolsquid/916e3be1e7dec0fcecca877e1aa323f1).

**Targets**
- The "dimension_travel" event now has a mob target. An example may be found [here](https://gist.github.com/coolsquid/754ddca67edeb2f52882a0800bb5d920).

**Target conditions**
- Added "min_food_level", "max_food_level", "min_saturation", and "max_saturation", all numbers.
