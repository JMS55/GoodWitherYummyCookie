## TODO
1. Remove boss bar after taming
2. Why aren't eating/taming particles appearing?
3. Make the wither behave tamed (follow/teleport to player, don't attack the player) (look at WolfEntity.java)
4. Check to make sure that the player dosen't already have a tamed wither when setting owner

## Bug fixes and improvements
1. Why does the wither get stuck on blocks during WitherEatCookieGoal?
2. Make WitherEatCookieGoal look at cookie (remember to change controls)
3. Test if startMovingTo() needs to be called in start() or tick() (will the wither track a falling cookie or one sliding on ice?)
4. Make eating sound/particles last for 1s
5. Instead of just setting the boss bar to not be visible, actually remove it when tamed

## Add later
1. Add wither upgrades
2. Add wither whistle
3. Add wither captivator
4. Add an advacement for taming a wither
5. Comment code
6. Add README.md
