# jsf-sample — Jakarta EE 10 / JSF サンプルプロジェクト

## スタック

| 項目 | バージョン |
|------|-----------|
| Jakarta EE | 10.0.0 |
| Jakarta Faces (JSF) | 4.0 |
| アプリケーションサーバー | WildFly 31.0.0.Final (JBoss EAP 8.0.6 相当) |
| JDK | OpenJDK 21 |
| ビルドツール | Maven 3.x (war パッケージング) |
| コンテナ | Docker / docker-compose |

---

## ビルド手順

```bash
mvn clean package
```

`target/jsf-sample.war` が生成されます。

---

## 起動手順

```bash
docker-compose up --build
```

初回はイメージのビルドを含むため数分かかります。  
`mvn clean package` を実行してから `docker-compose up --build` を行ってください。

---

## アクセス URL

| 用途 | URL |
|------|-----|
| アプリケーション | http://localhost:8080/jsf-sample/views/index.xhtml |
| 管理コンソール | http://localhost:9990 |

管理コンソールの認証情報: `admin` / `admin123`

---

## ディレクトリ構成

```
jsf-sample/
├── CLAUDE.md
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── src/
    └── main/
        ├── java/com/example/jsfsample/
        │   └── UserBean.java
        └── webapp/
            ├── WEB-INF/
            │   ├── web.xml          # FacesServlet マッピング
            │   ├── beans.xml        # CDI 有効化
            │   └── faces-config.xml # JSF 設定（最小構成）
            ├── index.xhtml          # 入力フォーム
            └── list.xhtml           # ユーザー一覧
```

---

## 命名規則

- **パッケージ名**: `com.example.jsfsample`（以下のパッケージ配置ルールに従う）
- **Managed Bean**: `@Named` + スコープアノテーション。クラス名は `XxxBean` とする
- **EL 式での参照名**: `@Named` 無指定の場合はクラス名の先頭小文字（例: `UserBean` → `userBean`）
- **XHTML ファイル名**: ケバブケース（例: `user-detail.xhtml`）
- **アクションメソッド**: 遷移先のビュー名（文字列）または `null`（同一ページ再表示）を返す

---

## パッケージ配置ルール

### Java（`src/main/java/com/example/jsfsample/`）

ドメインを第一階層、機能分類を第二階層以降とする。

```
com.example.jsfsample/
├── {ドメイン}/          # 例: user
│   ├── backing/        # 画面制御クラス（XHTML と 1:1、@RequestScoped）
│   ├── model/          # 値保持クラス・DTO（@FlowScoped / @SessionScoped / POJO）
│   └── service/        # 外部API呼び出しなど入出力を伴う処理
└── util/
    └── {機能名}/        # 例: filter — 業務ドメインに属さない横断的処理
```

| 分類 | 第一階層 | 第二階層 | 基準 |
|---|---|---|---|
| 業務ドメイン | ドメイン名（例: `user`） | `backing` / `model` / `service` | 業務の関心事に属するクラス |
| システム共通 | `util` | 機能名（例: `filter`） | 業務ドメインに属さない横断的処理 |

### webapp（`src/main/webapp/`）

```
webapp/
├── views/              # 画面 XHTML（フロー単位でサブディレクトリを切る）
│   ├── index.xhtml
│   ├── list.xhtml
│   └── register/
│       ├── register-input.xhtml
│       ├── register-confirm.xhtml
│       └── register-complete.xhtml
├── WEB-INF/
└── resources/
    ├── dads/           # 複合コンポーネント（JSF仕様により移動禁止）
    └── img/            # アプリ固有の画像
```

**重要**: `resources/dads/` は JSF の複合コンポーネント解決パスであり、**移動・リネーム禁止**。
`xmlns:dads="jakarta.faces.composite/dads"` の名前空間解決に使われるため、別のディレクトリに移動すると
コンポーネントが見つからずアプリが動作しなくなる。

---

## JSF 開発時の注意事項

### スコープと Serializable
- `@ViewScoped` / `@SessionScoped` / `@ConversationScoped` の Bean は必ず `Serializable` を実装すること
- `serialVersionUID` を明示的に定義することを推奨

### CDI スコープの選択指針
| スコープ | 用途 |
|---------|------|
| `@RequestScoped` | 1リクエストで完結する処理 |
| `@ViewScoped` | 同一ページ内でのAjax操作・フォーム検証 |
| `@SessionScoped` | ログイン情報など複数ページをまたぐ状態 |
| `@ApplicationScoped` | キャッシュなどアプリ全体の共有データ |

### リダイレクト
- POST-Redirect-GET パターンを使う場合は `?faces-redirect=true` をアクションメソッドの戻り値に付加する
  ```java
  return "list?faces-redirect=true";
  ```

### xmlns 名前空間（Faces 4.0）
Faces 4.0 では `javax.faces.*` が廃止され `jakarta.faces.*` に移行済み。

```xml
xmlns:h="jakarta.faces.html"
xmlns:f="jakarta.faces.core"
xmlns:ui="jakarta.faces.facelets"
```

### Faces 4.0 の変更点（JSF 2.x からの移行注意）
- `javax.*` パッケージはすべて `jakarta.*` へ
- `h:outputStylesheet` / `h:outputScript` の `target` 属性はそのまま使用可
- `faces-config.xml` の version は `4.0`、beans.xml の version は `4.0`

### WildFly デプロイ
- `standalone/deployments/` に `.war` を置くだけで自動デプロイされる
- デプロイ成功時は `.war.deployed` ファイルが生成される
- ログは `standalone/log/server.log` を確認

---

## 仕様駆動の進め方（docs/ ドキュメント管理）

`docs/` 配下の4ファイルで仕様と進捗を管理する。

| ファイル | 更新タイミング |
|---|---|
| `PLAN.md` | ユーザーが要件を追記・変更したとき |
| `SPEC.md` | PLAN.md の変更をユーザーと議論し、合意が取れたとき |
| `TODO.md` | SPEC.md の内容がユーザーとの議論で確定した後 |
| `KNOWLEDGE.md` | 実装中にハマった問題・解決策が判明したとき |

**重要**: `SPEC.md` の更新依頼を受けた場合、`TODO.md` は自動的に更新しないこと。
`TODO.md` はユーザーと SPEC の内容について議論が完了し、明示的に更新を依頼されてから変更する。

---

## アーキテクチャディシジョンレコード（ADR）

アーキテクチャ上の意思決定を伴う議論が完了したら、`docs/adr/` に Nygard 形式で ADR を作成すること。

**ADR を作成するタイミング**
- 複数の設計案を比較検討し、いずれかを採用・不採用にした場合
- スコープ・クラス設計・フレームワーク利用方針など、後から「なぜこうなったか」が不明瞭になりそうな決定をした場合

**ファイル命名**: `ADR-NNN-kebab-case-title.md`（NNN は連番）

**作成後**: `README.md` の ADR テーブルにエントリを追加すること。
