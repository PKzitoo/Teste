# ffkit-lite

Host overlay Android (arm64) com painel neon, container VA pra carregar
o APK original do Free Fire em sandbox, injecao in-process via `System.load`,
dumper de offsets il2cpp, mascara anti-detect de VA, aimbot (head-lock com
fov/suavidade/so-mirando/so-atirando/ignora-bots), ESP, no-recoil, speed/fly
e PANIC por volume-.

## Como compilar

Pre-requisitos (no PC):
- Android SDK + build-tools 34 + platform-34
- NDK r26b
- CMake 3.22
- Java 17
- `keytool -genkey -v -keystore keystore.jks -alias ffkit -keyalg RSA -keysize 2048 -validity 10000`

```bash
export ANDROID_SDK_ROOT=$HOME/Android/Sdk
export ANDROID_NDK_HOME=$ANDROID_SDK_ROOT/ndk/26.1.10909125
export KS_PASS=suaSenha
chmod +x scripts/*.sh
./scripts/build_apk.sh
```

Saida: `dist/ffkit.apk` (host disfarcado como "Calculadora").

## Como usar (resumo)

1. Instala `dist/ffkit.apk` no celular.
2. Abre "Calculadora", digita `1337`, aperta `=`.
3. Concede permissao de overlay.
4. No painel -> aba "Jogo" -> "importar APK do Free Fire" -> escolhe o APK
   extraido do seu celular via APK Extractor.
5. "abrir Free Fire" dentro do painel -> login normal -> joga.
6. Bolha neon flutua sobre o jogo; toque = painel.

## Anti-ban

- Zero root (sem magisk, sem zygisk, sem frida externo).
- APK original do FF intocado (SignatureSpoof do VA devolve a assinatura real).
- libffkit roda no mesmo processo do jogo (nao eh ptrace cross-process).
- Hooks de libc esterilizam `/proc/self/maps`, `cmdline`, `status`,
  `getppid`, `getprop ro.secure/ro.debuggable`.
- `FLAG_SECURE` bloqueia screenshot e gravacao de tela do painel.
- PANIC: tres toques em VOL- em 2s.

Config humana recomendada: aimbot "so mirando + so atirando",
suavidade >= 3, ignora bots, speed/voo NUNCA em ranked.
