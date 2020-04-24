## TODO
1. Modify goals to follow/teleport to player and attack owner's attacker when tamed (see FolowOwnerGoal, TrackOwnerAttackerGoal, and WitherEntity.tickMovement())
2. Check to make sure that the player dosen't already have a tamed wither when setting owner (use a mixin on PlayerEntity, and sync it to the wither's death)

## Bug fixes and improvements
1. Why does the wither get stuck trying to climb blocks during WitherEatCookieGoal?
2. Make sound/particles last for 1s and fix eating particle positions
3. Make WitherEatCookieGoal look at cookie
4. Test if startMovingTo() needs to be called in start() or tick() (will the wither track a falling cookie or one sliding on ice?)

## Add later
1. Add wither upgrades
2. Add wither whistle
3. Add wither idle animations
4. Add wither captivator
5. Add an advacement for taming a wither
6. Add all the other vanilla stuff like creative menus, etc
7. Comment code
8. Add README.md
9. Add CHANGELOG.md
10. Add more stuff related to withers
