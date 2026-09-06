# Neliofono (ネリオフォノ) 🎵📀

**Neliofono（ネリオフォノ）** は、ANBERNIC RG Rotateをはじめとする物理コントローラー搭載デバイス特化のレコードプレーヤー風音楽再生Androidアプリです。

「Neliö（フィンランド語で四角・スクエア）」の画面の中で、レコード円盤が美しく回転・入れ替わるギミックと、物理ボタンによる完全なハンズフリー/ゲームパッド操作体験を提供します。

---

## 🌟 特徴 (Features)

1. **RG Rotate 物理キー入力完全対応 (`KeyEventHandler`)**
   - フォーカス状態に依存せず、Activityレベルで直接物理キーイベントをインターセプト。
   - レコードプレーヤー操作（再生/停止、曲送り/戻し、プレイリスト表示切替）を物理ボタンに完全マッピング。
   - 押下キーのリアルタイムHUD表示＆Logcat出力。

2. **物理画面回転アダプティブレイアウト (`NeliofonoPlayerScreen`)**
   - RG Rotateの物理画面回転（縦・横・正方形）に対応。
   - **縦画面（Portrait）**: 上部に1:1比率のレコード盤、下部に曲情報・シークバー・操作ボタン・キーガイド。
   - **横画面（Landscape）**: 左側に1:1比率のレコード盤、右側に曲情報・シークバー・操作ボタン・キーガイド。

3. **リッチなレコード盤ビジュアル & アニメーション (`RecordDiscPlaceholder`)**
   - レコードの溝（Grooves）、光沢反射（Sheen）、45 RPM センターラベル、スピンドルホールをCompose Canvasで描画。
   - 再生/一時停止に連動したリアルタイム回転アニメーション。

4. **最新のメディア基盤準備**
   - Jetpack Media3 (ExoPlayer, Session, UI), Palette, Coil, Material 3 などのライブラリを組み込み済み。

---

## 🎮 物理キーマッピング (RG Rotate Controls)

| ボタン / キー | 動作 (Action) |
|---|---|
| **A ボタン** / `MEDIA_PLAY_PAUSE` / `Space` | 再生 / 一時停止 (Play / Pause) |
| **R1 ボタン** / `DPAD_RIGHT` (十字右) | 次の曲 (Next Track) |
| **L1 ボタン** / `DPAD_LEFT` (十字左) | 前の曲 (Previous Track) |
| **Y ボタン** | プレイリスト / ライブラリ切り替え (Toggle Playlist) |
| **B / X / START / SELECT / DPad** | キー検知ログ（Logcat & リアルタイムHUD）に即時反映 |

---

## 🛠 技術スタック (Tech Stack)

- **Language**: Kotlin 1.9.23
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + StateFlow / MVI Event Driven
- **Media Engine**: AndroidX Media3 (ExoPlayer 1.3.1, MediaSession)
- **Image & Palette**: Coil 2.6.0, AndroidX Palette 1.0.0
- **Min SDK**: 26 (Android 8.0 Oreo) / **Target SDK**: 34 (Android 14)

---

## 📁 プロジェクト構造

```text
neliofono/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/app/neliofono/
│       │   ├── MainActivity.kt
│       │   ├── input/
│       │   │   └── KeyEventHandler.kt          # RG Rotate物理キー捕捉・ディスパッチ
│       │   ├── model/
│       │   │   └── TrackInfo.kt                # データモデル & キーイベント定義
│       │   ├── ui/
│       │   │   ├── NeliofonoPlayerScreen.kt    # アダプティブレイアウト画面
│       │   │   ├── components/
│       │   │   │   ├── KeyGuideBar.kt          # 物理キーガイド & リアルタイムHUD
│       │   │   │   └── RecordDiscPlaceholder.kt# 1:1 レコード盤 & 回転描画
│       │   │   └── theme/
│       │   │       ├── Color.kt
│       │   │       ├── Theme.kt
│       │   │       └── Type.kt
│       │   └── viewmodel/
│       │       └── PlayerViewModel.kt          # 再生状態・キーログ管理
│       └── res/
│           └── values/
│               ├── strings.xml
│               └── themes.xml
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── .gitignore
└── README.md
```

---

## 🚀 ビルド & 実行方法 (Build & Run)

1. **Android Studio** でプロジェクトフォルダ（`neliofono`）を開きます。
2. Gradle Sync が完了するのを待ちます。
3. RG Rotate または Android エミュレータ / 実機を接続し、`app` を実行（Run）します。
4. キーボードやゲームパッド、画面タッチで操作を確認できます。
