# jsf-sample

Jakarta EE 10 / JSF 4.0 のサンプルアプリケーションです。  
名前とメールアドレスを登録してセッション中一覧表示する、最小構成のユーザー登録フォームです。

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
mvn clean package
docker compose up --build
```

---

## アクセス URL

| 用途 | URL |
|------|-----|
| ユーザー登録フォーム | http://localhost:8080/jsf-sample/index.xhtml |
| ユーザー一覧 | http://localhost:8080/jsf-sample/list.xhtml |
| WildFly 管理コンソール | http://localhost:9990 |

管理コンソールの認証情報: `admin` / `admin123`

---

## ディレクトリ構成

```
jsf-sample/
├── Dockerfile            # マルチステージビルド（Maven ビルド + WildFly 実行）
├── docker-compose.yml    # ポート 8080 / 9990 をホストに公開
├── pom.xml               # Jakarta EE 10 API (provided スコープ)
└── src/main/
    ├── java/com/example/jsfsample/
    │   └── UserBean.java         # ユーザー登録ロジック（SessionScoped Bean）
    └── webapp/
        ├── WEB-INF/
        │   ├── web.xml           # FacesServlet マッピング
        │   ├── beans.xml         # CDI 有効化
        │   └── faces-config.xml  # JSF 設定（最小構成）
        ├── index.xhtml           # ユーザー登録フォーム
        └── list.xhtml            # 登録済みユーザー一覧
```

---

## ソース概要

### `UserBean.java`

`@Named` + `@SessionScoped` の CDI Managed Bean。  
セッション中に登録したユーザーをメモリ上のリスト (`List<User>`) で保持します。

| メンバー | 説明 |
|---------|------|
| `name` / `email` | フォーム入力値のバインド先 |
| `users` | 登録済みユーザーのリスト |
| `register()` | ユーザーを追加して `list.xhtml` へリダイレクト（POST-Redirect-GET） |
| `User` (内部クラス) | 名前とメールを保持する不変データクラス |

### `index.xhtml` — ユーザー登録フォーム

- `#{userBean.name}` / `#{userBean.email}` にフォーム値をバインド
- 名前・メールアドレスは `required="true"` で入力必須
- 「登録」ボタンで `userBean.register()` を呼び出し、一覧ページへリダイレクト

### `list.xhtml` — ユーザー一覧

- `<ui:repeat>` で `#{userBean.users}` をテーブル表示
- ユーザーが 0 件の場合はメッセージを表示

---

## 画面フロー

```
index.xhtml（登録フォーム）
    ↓ 「登録」ボタン → userBean.register()
list.xhtml（ユーザー一覧）
    ↓ 「登録フォームへ戻る」リンク
index.xhtml
```

---

## ドキュメント

設計ドキュメントは `docs/` 配下で管理しています。

| ファイル | 内容 |
|---|---|
| [docs/PLAN.md](docs/PLAN.md) | 要件ダンプ |
| [docs/SPEC.md](docs/SPEC.md) | 画面・クラス・コンポーネント仕様 |
| [docs/TODO.md](docs/TODO.md) | タスク管理 |
| [docs/KNOWLEDGE.md](docs/KNOWLEDGE.md) | 実装ノウハウ・ハマりポイント |

### アーキテクチャディシジョンレコード（ADR）

アーキテクチャ上の意思決定の経緯は `docs/adr/` に Nygard 形式で記録しています。

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
3. 起動時に `standalone.sh` を実行（`0.0.0.0` バインドでコンテナ外からアクセス可能）

**docker-compose.yml**

- ホストの `8080` → コンテナの `8080`（アプリ）
- ホストの `9990` → コンテナの `9990`（WildFly 管理コンソール）
