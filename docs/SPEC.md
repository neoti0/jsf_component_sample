# SPEC.md — 仕様

## システム概要

Jakarta EE 10 / Faces 4.0 ベースのユーザー登録サンプルアプリ。
入力 → 確認 → 完了の3画面フローを JSF Faces Flow で管理し、
DADS（デジタル庁デザインシステム）を複合コンポーネントとして実装する。

---

## 画面フロー

```
register-input.xhtml
  （入力フォーム）
        ↓ 確認する
register-confirm.xhtml
  （入力確認）
    ↓ 登録する       ↓ 戻る
register-complete.xhtml   register-input.xhtml
  （完了）
        ↓ 一覧を見る
list.xhtml
  （登録済みユーザー一覧）
```

---

## 画面仕様

### register-input.xhtml — ユーザー登録（入力）

現状の `index.xhtml` を Flow 対応に移行する。

**フォーム構成**

| フィールド | 種別 | 必須 | バリデーション |
|---|---|---|---|
| 名前 | テキスト | ○ | `@NotBlank` |
| メールアドレス | テキスト | ○ | `@NotBlank`、`@Email` |
| 電話番号 | テキスト | ○ | `@NotBlank` |
| 郵便番号 | テキスト | ○ | `@NotBlank`、`@Pattern(\d{3}-?\d{4})` |
| 都道府県 | テキスト | ○ | `@NotBlank` |
| 市区町村 | テキスト | ○ | `@NotBlank` |
| 町村番地 | テキスト | ○ | `@NotBlank` |
| ビル・建物名 | テキスト | × | なし |

**郵便番号 + 住所エリア**
- `dads:addressField` 複合コンポーネントに切り出す（後述）

**ボタン**
- 「確認する」ボタン → バリデーション通過後 `register-confirm.xhtml` へ遷移

---

### register-confirm.xhtml — ユーザー登録（確認）

入力内容を読み取り専用で表示し、確定かキャンセルを選択させる。

**表示構成**
- 入力画面と同じ項目を `dads:outputField` 複合コンポーネントで表示
- 住所は「郵便番号 / 都道府県・市区町村・町村番地 / ビル建物名」の順に表示

**ボタン**
- 「登録する」ボタン → `registerCompleteController.register()` 呼び出し → `register-complete.xhtml` へ遷移
  - このタイミングで `UserFormData` の内容を `UserListBean.users` に追加する
- 「戻る」ボタン → `register-input.xhtml` へ戻る（Flow 内遷移。入力値は保持）

---

### register-complete.xhtml — ユーザー登録（完了）

登録受け付けの旨を伝える完了画面。

**表示構成**
- 「ご登録ありがとうございました」旨の文言
- 感謝を伝えるイラスト（`dads:illustration` 複合コンポーネントで表示）
- 「一覧を見る」リンク → `list.xhtml` へ遷移（Flow を抜ける）

---

### list.xhtml — 登録済みユーザー一覧（既存）

変更なし。`UserListBean.users` を表示。

---

## クラス設計

### 責務分割方針

| 種別 | 役割 | スコープ |
|---|---|---|
| モデルクラス | フィールド定義・Bean Validation | `@FlowScoped` |
| Backing Bean | 画面操作・API 呼び出し（XHTML と 1:1） | `@RequestScoped` |
| 一覧管理クラス | 登録済みユーザーのリスト保持 | `@SessionScoped` |

- **フォームは `UserFormData` のフィールドに直接バインド**する（`#{userFormData.name}` 形式）
- BV は `UserFormData` のアノテーションに集約。Backing Bean には BV を書かない
- Backing Bean はナビゲーションメソッドと `@Inject` のみを持つ薄い実装にする

### クラス一覧

```
com.example.jsfsample/
├── model/
│   ├── UserFormData.java          @Named @FlowScoped — ユーザー情報（AddressFormData を内包）
│   └── AddressFormData.java       POJO — 住所情報 + 住所関連 BV アノテーション
├── backing/
│   ├── RegisterInputBacking.java  @Named @RequestScoped — 入力画面（1:1）
│   ├── RegisterConfirmBacking.java @Named @RequestScoped — 確認画面（1:1）
│   └── RegisterCompleteBacking.java @Named @RequestScoped — 完了画面（1:1）
└── list/
    └── UserListBean.java          @Named @SessionScoped — 登録済みユーザー一覧
```

### AddressFormData（POJO）

住所に関するフィールドと BV アノテーションを集約したモデルクラス。
CDI スコープは持たず、`UserFormData` にフィールドとして内包される。
`dads:addressField` は このクラスのインスタンスを受け取ることで、`UserFormData` に依存しない。

```java
public class AddressFormData implements Serializable {

    @NotBlank(message = "郵便番号は必須です")
    @Pattern(regexp = "\\d{3}-?\\d{4}", message = "郵便番号の形式が正しくありません（例: 100-0004）")
    private String postalCode;

    @NotBlank(message = "都道府県は必須です")
    private String prefecture;

    @NotBlank(message = "市区町村は必須です")
    private String city;

    @NotBlank(message = "町村番地は必須です")
    private String streetAddress;

    private String buildingName;  // 任意
}
```

### UserFormData（@Named @FlowScoped）

ユーザー情報全体を管理する Flow スコープのモデルクラス。
住所フィールドは `AddressFormData` に委譲し、`@Valid` で BV を連鎖させる。

```java
@Named
@FlowScoped("register")
public class UserFormData implements Serializable {

    @NotBlank(message = "名前は必須です")
    private String name;

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;

    @NotBlank(message = "電話番号は必須です")
    private String phoneNumber;

    @Valid
    private AddressFormData address = new AddressFormData();
}
```

**フォームバインドの例**

```xml
<!-- 個別フィールドは userFormData.address 経由 -->
<dads:inputField id="name"   label="名前"           value="#{userFormData.name}" />
<dads:inputField id="email"  label="メールアドレス"  value="#{userFormData.email}" />

<!-- 住所エリアは AddressFormData を丸ごと渡す -->
<dads:addressField value="#{userFormData.address}" />
```

### Backing Bean（例：RegisterConfirmBacking）

状態を持たない薄い実装。ナビゲーションと `@Inject` のみ。

```java
@Named
@RequestScoped
public class RegisterConfirmBacking {

    @Inject UserFormData userFormData;
    @Inject UserListBean userListBean;

    public String register() {
        userListBean.add(userFormData);  // セッションスコープのリストへ格納
        return "register-complete";      // Flow 内遷移
    }

    public String back() {
        return "register-input";         // Flow 内遷移（入力値は FlowScoped に残る）
    }
}
```

### XHTML でのバインド例

フォームフィールドは `userFormData` を直接参照する。

```xml
<dads:inputField id="name"
                 label="名前"
                 value="#{userFormData.name}" />
```

### Flow 定義

`faces-config.xml` または `@Produces @FlowDefinition` で定義。

- Flow ID: `register`
- 開始ノード: `register-input`
- ノード: `register-input` → `register-confirm` → `register-complete`
- リターンノード: `register-complete` の「一覧を見る」で Flow を終了し `list` へ

---

## 複合コンポーネント仕様

### 既存

| コンポーネント | 役割 | 変更 |
|---|---|---|
| `dads:inputField` | ラベル＋入力欄＋エラー表示 | なし |
| `dads:button` | DADS ボタン（Ajax 対応） | なし |

### 新規追加

#### dads:table — DADS スタイルテーブル

ヘッダー行と明細行をそれぞれ配列で受け取り、DADS スタイルのテーブルを描画する汎用コンポーネント。
XHTML 側には `dads:*` コンポーネントのみを記述させ、`h:column` 等の JSF レイアウト部品を呼び出し元に露出しない。
レイアウトの複雑さはコンポーネント内部に閉じ込める。

| 属性 | 型 | 必須 | 説明 |
|---|---|---|---|
| `headers` | `List<String>` | ○ | ヘッダー行のラベル一覧（例: `["名前", "メール", ...]`）|
| `rows` | `List<List<String>>` | ○ | 明細行。各要素が1行分の値リスト |

**呼び出し例（XHTML）**

```xml
<dads:table headers="#{userListBacking.tableHeaders}"
            rows="#{userListBacking.tableRows}" />
```

**責務の分担方針**

| 処理 | 担当 |
|---|---|
| フィールドの結合・列順制御・型変換 | Backing Bean |
| 空値・null 時の表示（例: `—`）など汎用的な表示ロジック | `dads:table` コンポーネント内部 |

コンポーネントはデータの意味を関知しない。汎用的な表示制御のみを内包する。

**データ整形責務（Backing Bean 側）**

文字列への変換・列順の制御は Backing Bean が担う。

```java
public List<String> getTableHeaders() {
    return List.of("名前", "メールアドレス", "電話番号", "郵便番号", "住所");
}

public List<List<String>> getTableRows() {
    return userListBean.getUsers().stream()
        .map(u -> List.of(
            u.getName(),
            u.getEmail(),
            u.getPhoneNumber(),
            u.getPostalCode(),
            u.getPrefecture() + u.getCity() + u.getStreetAddress()
        ))
        .toList();
}
```

**内部構成（コンポーネント）**

`ui:repeat` を二重ネストしてヘッダー・明細を描画する。

```xml
<div id="#{cc.clientId}" class="dads-table">
    <table class="dads-table__table">
        <thead>
            <tr>
                <ui:repeat value="#{cc.attrs.headers}" var="header">
                    <th class="dads-table__col-header" scope="col">#{header}</th>
                </ui:repeat>
            </tr>
        </thead>
        <tbody>
            <ui:repeat value="#{cc.attrs.rows}" var="row">
                <tr>
                    <ui:repeat value="#{row}" var="cell">
                        <td>#{cell}</td>
                    </ui:repeat>
                </tr>
            </ui:repeat>
        </tbody>
    </table>
</div>
```



#### dads:addressField — 郵便番号検索＋住所入力

郵便番号入力・検索ボタン・住所フィールド（都道府県〜ビル名）を一体化した**カスタムコンポーネント**（Java 実装）。
複合コンポーネント（XHTML ベース）との良し悪しを比較・検証することを目的として採用する。
呼び出し元 XHTML はモデルを1つ渡すだけでよく、内部のフィールド構成を知る必要がない。

| 属性 | 型 | 必須 | 説明 |
|---|---|---|---|
| `value` | `AddressFormData` | ○ | 住所モデル（例: `#{userFormData.address}`）|

**呼び出し例（XHTML）**

```xml
<dads:addressField value="#{userFormData.address}" />
```

**実装クラス構成**

| クラス | 役割 |
|---|---|
| `AddressFieldComponent` | `UIInput` を継承。`@FacesComponent` で登録 |
| `AddressFieldRenderer` | HTML レンダリングを担当。`@FacesRenderer` で登録 |
| `AddressSearchService` | `@Named @RequestScoped`。API 呼び出しと `AddressFormData` への結果格納 |

**検索結果に応じた振る舞い**

| 結果件数 | 振る舞い |
|---|---|
| 0件 | 住所フィールドは更新しない。エラーメッセージを表示 |
| 1件 | `AddressFormData` に自動入力 |
| 2件以上 | 候補一覧をモーダルで表示し、ユーザーに選択させる |

**モーダルの仕様**
- 候補住所をラジオボタンで一覧表示（例: `○ 愛知県 弥富市` / `○ 三重県 桑名郡木曽岬町`）
- 「確定」ボタン押下で選択した住所を `AddressFormData` に反映してモーダルを閉じる
- 「キャンセル」ボタン押下でモーダルを閉じ、住所フィールドは変更しない

**データフロー**

```
[住所検索ボタン] Ajax
  ↓
AddressSearchService.search(postalCode)
  ↓ List<AddressCandidate> を返す
  ├─ 0件 → エラーメッセージ表示
  ├─ 1件 → AddressFormData に自動入力
  └─ 2件以上 → 候補リストをコンポーネントの状態に保持
               → モーダルを表示（Ajax 再描画）
                    ↓ ラジオボタンで選択 → 「確定」Ajax
               AddressFormData に選択した候補を反映
               → モーダルを閉じ、住所フィールドを更新
```

**AddressCandidate（POJO）**

候補住所を表すデータクラス。`AddressSearchService` が返す単位。

```java
public class AddressCandidate {
    private String prefecture;
    private String city;
    private String streetAddress;
}
```

**コンポーネントの状態管理**

モーダル表示中は候補リストをコンポーネントの状態（`saveState` / `restoreState`）で保持する。
Ajax リクエストをまたいで候補リストを維持するために、`UIComponentBase` の状態保存機構を利用する。

**内部構成**
- `AddressFieldRenderer` が郵便番号入力欄・検索ボタン・住所フィールド・モーダルの HTML を生成する
- BV アノテーションは `AddressFormData` のフィールドに定義。`UserFormData` の `@Valid` で連鎖適用される

**AddressSearchService（CDI Bean）**
- `@Named @RequestScoped`
- `search(postalCode)` メソッドで API を呼び出し、`List<AddressCandidate>` を返す
- 現状はダミー実装。実 API（zipcloud 等）に差し替え可能な設計にする

#### dads:outputField — 確認画面用ラベル表示

確認画面で入力内容を読み取り専用で表示するコンポーネント。

| 属性 | 型 | 必須 | 説明 |
|---|---|---|---|
| `label` | String | ○ | ラベルテキスト |
| `value` | String | ○ | 表示する値 |

**表示構成**
- ラベル（DADS スタイル）＋ 値テキスト
- 入力欄ではなく `h:outputText` で表示

#### dads:illustration — イラスト表示

完了画面でイラストを表示するコンポーネント。

| 属性 | 型 | デフォルト | 説明 |
|---|---|---|---|
| `src` | String | ○ | 画像パス（コンテキストパス相対）|
| `alt` | String | `""` | alt テキスト |
| `size` | String | `md` | `sm` / `md` / `lg` |

---

## バリデーション方針

- バリデーション定義は `UserFormData` のフィールドアノテーションに集約
- JSF の `required` 属性は使用しない（`dads:inputField` の `required` 属性はバッジ表示のみ）
- `web.xml` に `jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL=true` を設定

---

## スコープ管理

| データ | スコープ | 理由 |
|---|---|---|
| `UserFormData`（フォームデータ + BV） | `@FlowScoped` | 登録3画面で共有。Flow 終了時に破棄 |
| `UserListBean.users`（登録済みリスト） | `@SessionScoped` | ブラウザセッション全体で保持 |
| Backing Bean 各クラス | `@RequestScoped` | 状態なし。ナビゲーションのみ |

---

## 環境・起動

```bash
docker compose up --build
```

- アクセス: http://localhost:8080/jsf-sample/index.xhtml（暫定。Flow 対応後は register-input.xhtml）
