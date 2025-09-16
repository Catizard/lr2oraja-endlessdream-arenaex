# LR2oraja \~Endless Dream\~ ArenaEx

This is an unauthorized, hand-crafted [LR2ArenaEx](https://github.com/SayakaIsBaka/LR2ArenaEx) port. And based on Endless Dream's dev branch(`0.3.0`).

In short, this repo brings the functionality:

- As a client join other LR2ArenaEx's server session
- Can play with LR2 users
- Host as a server

And will be merged into endless dream's mainline once it's done

To build this branch, follow the instruction from upstream's README and rememeber to upgrade the jdk version to 17 at least.

## Development

Need some time to polish this project...

- [x] Porting whole server side
    - [ ] The server side is far from usable, need some time to test 
- [ ] Play scene
    - [x] Auto select the host's song(see note below)
    - [x] A/AA/AAA limits are miscalculated
    - [x] The height of graph menu should be adjusted. I don't know why there's a horizontal bar
    - [x] Kick users/Give host to other users are not implemented yet
    - [ ] Hijack the IR display to render lobby result instead
    - [ ] The play result should be reversed
    - [ ] Cancel event seems not sending
    - [ ] Hijack the pace maker to see lobby's status(optional)
- [ ] Settings
    - [ ] Items are not implemented
    - [ ] Random seed is not implemented
    - [ ] Option & Gauge are not implemented

## Other things you might want ask

Q: How do I pick a song/get ready?
A: Just launch the bms as usual. If you are the lobby's host, then the song would be picked for the lobby. For other users, you'll be lead to the song lobby choose if possible.

Q: What if auto select failed?
A: Try pressing the "Jump" button in arena menu

Q: What's the current progress?  
A: Only the basic functionality of client side has been implemented. Server side 0%.

Q: Does this branch has any relationship with lr2oraja arena or something similar?  
A: No. This branch is only a code port result from LR2ArenaEx.

Q: Why this code is a branch not a standalone repo?(outdated)
A: It's a standalone repo now because I find that github action is only triggered on main branch...

Q: Does this branch allows IR connections?  
A: Technically yes but this branch is based on dev branch of Endless Dream. So I think I'll disallow the connection in the near future.
