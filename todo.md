## TODO
1. Make the wither behave tamed (follow/teleport to player, don't attack the player)
    * Do I need to modify RevengeGoal? probably
    * Add goals that only start when tamed (and stop when not tamed, just in case)
2. Why aren't eating/taming particles appearing?
3. Check to make sure that the player dosen't already have a tamed wither when setting owner

## Bug fixes and improvements
1. Why does the wither get stuck on blocks during WitherEatCookieGoal?
2. Make WitherEatCookieGoal look at cookie (remember to change controls)
3. Test if startMovingTo() needs to be called in start() or tick() (will the wither track a falling cookie or one sliding on ice?)
4. Make eating sound/particles last for 1s

## Add later
1. Add wither upgrades
2. Add wither whistle
3. Add wither captivator
4. Add an advacement for taming a wither
5. Comment code
6. Add README.md
