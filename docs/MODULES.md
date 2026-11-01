# 47 функций Turik

Краткий справочник. Полное описание (логика, настройки, значения, статусы) — в чате
разработки; статусы и ограничения — в docs/TESTING.md и docs/LIMITATIONS.md.

## Combat
1. **AimAssist** — плавный доворот прицела на цель. Strength 60%, Radius 40°, Turn speed 35°/т, Range 4.2м, Aim part, Ignore friends, Visible only.
2. **TriggerBot** — автоатака под прицелом. Mode Normal/Only Crits/Smart, Delay 120ms, Range 4.5м, Check visibility, Ignore friends, Respect cooldown.
3. **Velocity** — модификация отбрасывания. Horizontal/Vertical 0%, Explosion H/V 0%, Apply only when enabled.
4. **AutoTotem** — тотем в левую руку. Health threshold 12HP, Only under threshold, Check remaining (Min 1), Cooldown 250ms.
5. **AutoArmor** — лучшая броня по характеристикам. Delay 300ms, Prefer durability, Work with any screen.
6. **AutoWeapon** — выбор оружия. Priority Auto/Sword/Axe, Range 4.0м, Only with target.
7. **NoFriendDamage** — отмена атак по друзьям. Also block fishing rod, Notify when blocked, Friend color.

## Movement
8. **Spider** — лазание по стенам. Mode Climb/Jump, Climb speed 0.28, Jump height 0.42.
9. **Jesus** — поведение на воде. Mode Solid/Hop/Swim, Surface offset 0.08, Stabilize.
10. **NoSlow** — снятие замедления. Shield, Bow/Crossbow, Food, Other items.
11. **InvMove** — движение в инвентаре. Speed 0.22, Jump, Sprint.
12. **ElytraSwap** — смена элитры/нагрудника. Swap key, Prefer Elytra/Chestplate, Delay 150ms.
13. **Flight** — полёт. Mode Velocity/Vanilla, Speed 1.2, Vertical speed 0.8, Anti-kick.
14. **Speed** — скорость. Mode Strafe/Sprint/Ground, Multiplier 1.35, Only forward.
15. **SafeWalk** — защита от схода с края. Include sneaking, Include jump padding.

## Player
16. **AutoRespawn** — авто-возрождение. Delay 1000ms.
17. **FastPlace** — быстрая установка блоков. Mode Spam/Hold, Delay 100ms, Only blocks, Respect max reach.
18. **FastBreak** — ускоренное копание. Mode Multiplier/Instant, Extra ticks 4x.
19. **NoFall** — без урона от падения. Mode Packet/Damping, Fall threshold 2.5, Skip in water/vehicle.
20. **AntiAFK** — против АФК-кика. Mode Swing/Rotate/Jump/Combo, Interval 20с, Turn angle 12°.
21. **AutoFish** — рыбалка. Recast delay 800ms, Notification, Auto recast.
22. **ChestStealer** — перенос из сундука. Filter All/Valuables, Delay 60ms, Skip weapons/armor, Close when empty.
23. **AutoEat** — автоеда. Hunger threshold 14, Prefer best food, Only edible items.
24. **ItemScroller** — колёсико в контейнерах. Items per tick 3, Invert direction, Pause on hover.
25. **FastUse** — быстрое использование. Items All/Throwables, Cooldown 1т, Feedback.

## Visuals
26. **PlayerESP** — выделение игроков. Through walls, Outline, Fill, Glow, Color, Friend color, Distance 64м, Line width 1.5.
27. **Nametags** — таблички над игроками. Text size 8, Distance 64м, Health, Armor, Distance, Friend color, Background.
28. **ItemESP** — подсветка предметов. Mode Through Walls/Visible, Outline, Fill, Color, Fill alpha 30%, Distance 48м.
29. **BlockESP** — подсветка типов блоков. Blocks (список id), Color, Outline, Fill, Radius 16м, Refresh 5т.
30. **FullBright** — яркость. Brightness 10, Smooth transition.
31. **NoRender** — отключение визуала. Fire overlay, Rain, Snow, Explosion particles.
32. **Tracers** — линии к сущностям. Target Players/Hostile/All, Color, Line width 1.5, Distance 64м, To feet.
33. **StorageESP** — подсветка хранилищ. Chest/Shulker/Other colors, Fill, Through walls, Distance 64м.
34. **EntityESP** — подсветка мобов. Filter All/Hostile/Passive, Hostile/Passive colors, Outline, Fill, Distance, Through walls.
35. **Trajectories** — траектории снарядов. Path color, Landing color, Landing point.

## World
36. **Ambience** — атмосфера мира. Sky color, Intensity 60%, Brighter world.
37. **Atmosphere** — пресеты Winter/Autumn/Mars/Saturn. Intensity 70%, Particles 120, Snow/Leaf colors.
38. **Weather** — погода. Weather Clear/Rain/Thunder, Intensity 60%, Thunder flashes.
39. **TimeChanger** — время суток. Mode Day/Night/Sunrise/Sunset/Manual, Manual time 12000.
40. **Fog** — дымка. Density 30%, Horizon height 0.55, Color, Label.
41. **XRay** — выделение руд через стены. Blocks (список id), Color, Opacity 70%, Radius 16м, FullBright assist.

## Misc
42. **AutoGG** — сообщение после раунда. Message "GG", Delay 1500ms, Trigger on death, Once per round.
43. **Sounds** — звуковые уведомления. Volume 40%, Module toggle, Friend change, Notifications.
44. **MiddleClickFriend** — друзья средней кнопкой. Notify, Only players.
45. **Watermark** — брендинг HUD. Color, Text size 9, FPS, Ping, Server, Time, Coordinates.
46. **AutoSwap** — переключение предметов. Item A/B (id), Swap key, Condition Manual/Auto empty, Delay 100ms.
47. **Friends** — FriendManager. Friend color, Show list on enable. Общий список: AimAssist, TriggerBot, NoFriendDamage, MiddleClickFriend, ESP-фильтры, Nametags.