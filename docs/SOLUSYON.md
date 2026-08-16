# ✅ Mga Problema at Solusyon

> Huling Inilathala: 2026-08-16 18:00 UTC

---

## ❌ Problema 1: Token na nakalagay sa loob ng file
> **Dahilan:** Nakasulat ang Token nang direkta sa loob ng workflow file → Hinarang ng GitHub
>
> ✅ **Solusyon:** Hindi na ilalagay ang Token sa loob ng anumang file → Secret lamang sa GitHub → Settings → Secrets → Actions → `TOKEN`

## ❌ Problema 2: Hindi nagsusulat sa errors.md kahit tumataakbo
> **Dahilan:** Maling kondisyon — hindi gumagana ang `steps.build.outcome` kapag may `continue-on-error`
>
> ✅ **Solusyon:** `if: always()` — TUMATAKBO KAHIT ANONG MANGYARI! Walang harang! Kahit tagumpay man o nabigo — isusulat pa rin!

## ❌ Problema 3: Hindi gumagana ang API call
> **Dahilan:** Maling format ng tawag at pahintulot
>
> ✅ **Solusyon:** Balik sa simpleng paraan — gamitin ang kusang `GITHUB_TOKEN` ng GitHub — may pahintulot na, siguradong gumagana!

## ❌ Problema 4: Hindi makita ang `/website` sa GitHub Pages
> **Dahilan:** Hindi pa naipadala ang folder, o hindi babasahin ang tamang folder
>
> ✅ **Solusyon:** Ilipat lahat sa `/docs` — iyan ang default na binabasa ng GitHub Pages — siguradong makikita!

---

> ✅ **LAHAT NG PROBLEMA — MAY SOLUSYON NA! WALANG NAKALIMUTAN! PASULONG NA LANG!**
