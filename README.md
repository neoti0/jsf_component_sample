# jsf-sample

Jakarta EE 10 / JSF 4.0 のサンプルアプリケーションです。  
DADS（デジタル庁デザインシステム）を JSF 複合コンポーネントとして実装し、
入力 → 確認 → 完了の3画面 Faces Flow でユーザー登録を行うサンプルです。

---

## 技術スタック

| 項目 | バージョン |
|------|-----------|
| Jakarta EE | 10.0.0 |
| Jakarta Faces (JSF) | 4.0 |
| アプリケーションサーバー | WildFly 31.0.0.Final |
| JDK | OpenJDK 21 |
| ビルドツール | Maven 3.x |
| コンテナ | Docker (Compose Plugin) |

---

## 起動方法

### 前提条件

- Docker Desktop がインストールされ起動していること（Compose Plugin 同梱）
- Maven 3.x がインストールされていること（ローカルビルドする場合）

### Docker で起動する（推奨）

```bash
docker compose up --build
```

Dockerfile 内でマルチステージビルドを行うため、Maven のインストールは不要です。  
初回はイメージのビルドに数分かかります。

### ローカルでビルドしてから起動する場合

```bash
JAVA_HOME=/path/to/jdk21 mvn clean package
docker compose up --build
```

---

## アクセス URL

| 用途 | URL |
|------|-----|
| トップ（フロー入口） | http://localhost:8080/jsf-sample/index.xhtml |
| 登録済みユーザー一覧 | http://localhost:8080/jsf-sample/list.xhtml |
| WildFly 管理コンソール | http://localhost:9990 |

管理コンソールの認証情報: `admin` / `admin123`

---

## 画面フロー

```
index.xhtml（トップ）
    ↓ 「ユーザー登録フォームへ」ボタン（Faces Flow 開始）
register-input.xhtml（登録入力）
    ↓ 「確認する」ボタン
register-confirm.xhtml（登録確認）
    ↓ 「登録する」       ↓ 「戻る」
register-complete.xhtml  register-input.xhtml
（登録完了）
    ↓ 「一覧を見る」（Flow 終了）
list.xhtml（登録済みユーザー一覧）
```

---

## ディレクトリ構成

```
jsf-sample/
├── Dockerfile                  # マルチステージビルド（Maven + WildFly）
├── docker-compose.yml          # ポート 8080 / 9990 公開
├── pom.xml                     # Jakarta EE 10 API (provided スコープ)
└── src/main/
    ├── java/com/example/jsfsample/
    │   ├── backing/             # Backing Bean（画面と 1:1、@RequestScoped）
    │   │   ├── RegisterInputBacking.java
    │   │   ├── RegisterConfirmBacking.java
    │   │   └── RegisterCompleteBacking.java
    │   ├── model/               # モデル / DTO（BV アノテーション集約）
    │   │   ├── UserFormData.java        @Named @FlowScoped
    │   │   ├── AddressFormData.java     POJO（住所情報）
    │   │   └── AddressCandidate.java    POJO（住所候補）
    │   ├── list/                # 一覧管理
    │   │   ├── UserListBean.java        @Named @SessionScoped
    │   │   └── UserListBacking.java     @Named @RequestScoped（テーブル整形）
    │   ├── service/             # 外部 API 呼び出し
    │   │   └── AddressSearchService.java  @Named @RequestScoped（zipcloud API）
    │   ├── component/           # JSF カスタムコンポーネント
    │   │   └── AddressFieldComponent.java  @FacesComponent（状態管理）
    │   └── filter/              # サーブレットフィルター
    │       └── CharacterEncodingFilter.java  @WebFilter（UTF-8 強制）
    └── webapp/
        ├── WEB-INF/
        │   ├── web.xml           # FacesServlet・CLIENT_WINDOW_MODE・文字コード
        │   ├── beans.xml         # CDI 有効化
        │   └── faces-config.xml  # Faces Flow (register) 定義
        ├── css/
        │   └── dads.css          # DADS スタイル
        ├── img/
        │   └── complete.svg      # 登録完了イラスト
        ├── resources/dads/       # DADS 複合コンポーネント
        │   ├── inputField.xhtml
        │   ├── button.xhtml
        │   ├── addressField.xhtml
        │   ├── outputField.xhtml
        │   ├── table.xhtml
        │   └── illustration.xhtml
        ├── index.xhtml           # トップ（フロー入口）
        ├── register-input.xhtml  # 登録入力画面（Flow ノード）
        ├── register-confirm.xhtml # 登録確認画面（Flow ノード）
        ├── register-complete.xhtml # 登録完了画面（Flow ノード）
        └── list.xhtml            # 登録済みユーザー一覧
```

---

## クラス概要

### モデル

| クラス | スコープ | 説明 |
|--------|---------|------|
| `UserFormData` | `@FlowScoped("register")` | フロー全体で共有するフォームデータ。BV アノテーションを集約 |
| `AddressFormData` | POJO | 住所フィールドと住所関連 BV アノテーション |
| `AddressCandidate` | POJO | zipcloud API から返却される住所候補 |

### Backing Bean

| クラス | スコープ | 説明 |
|--------|---------|------|
| `RegisterInputBacking` | `@RequestScoped` | 入力画面と 1:1。`confirm()` ナビゲーション |
| `RegisterConfirmBacking` | `@RequestScoped` | 確認画面と 1:1。`register()` / `back()` |
| `RegisterCompleteBacking` | `@RequestScoped` | 完了画面と 1:1。`toList()` で Flow 終了 |
| `UserListBean` | `@SessionScoped` | 登録済みユーザーリストを保持 |
| `UserListBacking` | `@RequestScoped` | 一覧画面向けにテーブルデータを整形 |

### コンポーネント・サービス

| クラス | 説明 |
|--------|------|
| `AddressFieldComponent` | `UIInput` 継承のカスタムコンポーネント。郵便番号検索・住所候補選択の状態を管理 |
| `AddressSearchService` | zipcloud API（`https://zipcloud.ibsnet.co.jp/api/search`）を呼び出し、住所候補を返す |
| `CharacterEncodingFilter` | 全リクエストに UTF-8 エンコードを強制するサーブレットフィルター |

---

## DADS 複合コンポーネント

| コンポーネント | 属性 | 説明 |
|---|---|---|
| `dads:inputField` | `label` / `value` / `required` | ラベル＋入力欄＋エラーメッセージ |
| `dads:button` | `value` / `action` / `buttonType` / `size` / `ajaxExecute` / `ajaxRender` | DADS スタイルボタン（Ajax 対応） |
| `dads:addressField` | `value`（AddressFormData） | 郵便番号検索＋住所フィールド一体型（Java カスタムコンポーネント） |
| `dads:outputField` | `label` / `value` | 確認画面用ラベル＋値の読み取り専用表示 |
| `dads:table` | `headers` / `rows` | DADS スタイルテーブル（ヘッダー＋明細行） |
| `dads:illustration` | `src` / `alt` / `size` | 完了画面用イラスト表示 |

---

## ドキュメント

| ファイル | 内容 |
|---|---|
| [docs/PLAN.md](docs/PLAN.md) | 要件ダンプ |
| [docs/SPEC.md](docs/SPEC.md) | 画面・クラス・コンポーネント仕様 |
| [docs/TODO.md](docs/TODO.md) | タスク管理 |
| [docs/KNOWLEDGE.md](docs/KNOWLEDGE.md) | 実装ノウハウ・ハマりポイント |

### アーキテクチャディシジョンレコード（ADR）

| ADR | タイトル | ステータス |
|---|---|---|
| [ADR-001](docs/adr/ADR-001-bv-annotations-on-flowscoped-model.md) | Bean Validation アノテーションを FlowScoped モデルクラスに集約する | Accepted |
| [ADR-002](docs/adr/ADR-002-backing-bean-requestscoped-no-snapshot.md) | Backing Bean を @RequestScoped とし、ViewScoped スナップショット方式を採用しない | Accepted |
| [ADR-003](docs/adr/ADR-003-addressfield-custom-component.md) | dads:addressField をカスタムコンポーネントで実装する | Accepted |
| [ADR-004](docs/adr/ADR-004-extract-address-form-data.md) | 住所情報を AddressFormData として UserFormData から切り出す | Accepted |

---

## Docker 構成の概要

**Dockerfile（マルチステージビルド）**

1. `maven:3.9-eclipse-temurin-21` イメージで `mvn clean package` を実行し WAR を生成
2. `eclipse-temurin:21-jre` イメージに WildFly 31 をインストールし、WAR を `standalone/deployments/` に配置
3. `LANG=en_US.UTF-8` / `JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8` を設定して UTF-8 を保証
4. 起動時に `standalone.sh` を実行（`0.0.0.0` バインドでコンテナ外からアクセス可能）

**docker-compose.yml**

- ホストの `8080` → コンテナの `8080`（アプリ）
- ホストの `9990` → コンテナの `9990`（WildFly 管理コンソール）
