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
| アプリケーション | http://localhost:8080/jsf-sample/index.xhtml |
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

- **パッケージ名**: `com.example.jsfsample`（機能追加時はサブパッケージを切る）
- **Managed Bean**: `@Named` + スコープアノテーション。クラス名は `XxxBean` とする
- **EL 式での参照名**: `@Named` 無指定の場合はクラス名の先頭小文字（例: `UserBean` → `userBean`）
- **XHTML ファイル名**: ケバブケース（例: `user-detail.xhtml`）
- **アクションメソッド**: 遷移先のビュー名（文字列）または `null`（同一ページ再表示）を返す

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
