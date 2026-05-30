# KNOWLEDGE.md — 学びの蓄積

## JSF 複合コンポーネントと Ajax render

**問題**: `dads:inputField` を `ajaxRender` の対象に指定しても画面が更新されなかった。

**原因**: JSF の Ajax partial render はレスポンスの `<update id="...">` に対応する DOM 要素を id で探す。
複合コンポーネントの `cc:implementation` のルート要素に id がないと、対象要素が見つからず更新が静かに失敗する。

**解決策**: `cc:implementation` のルート要素に `id="#{cc.clientId}"` を付与する。

```xml
<div id="#{cc.clientId}" class="dads-form-control-label" ...>
```

`cc.clientId` は JSF が割り当てるクライアント ID（例: `registerForm:prefecture`）を返す。

---

## h:message の Ajax 再描画

**問題**: Ajax リクエスト後に `h:message` のバリデーションエラーが表示されなかった。

**原因**: `h:message` が `ajaxRender` の対象に含まれていなかった。
`<span>` タグは JSF コンポーネントではないため id が付かず、render 対象に指定できない。

**解決策**: `h:message` を `<h:panelGroup id="postalCodeInput">` でラップし、その id を `ajaxRender` に追加する。

```xml
<h:panelGroup id="postalCodeInput" styleClass="dads-input-text">
    <h:inputText id="postalCode" ... />
    <h:message for="postalCode" ... />
</h:panelGroup>
```

---

## Bean Validation で @NotBlank と @Pattern が二重発火する

**問題**: 空欄で送信すると「郵便番号は必須です」と「形式が正しくありません」が両方表示された。

**原因**: Bean Validation はデフォルトで全制約を同時評価する。
空文字列 `""` は `@NotBlank`（空は NG）にも `@Pattern`（空は正規表現不一致）にも引っかかる。

**解決策**: `web.xml` に以下を追加して空文字列を `null` に変換する。
`null` は `@Pattern` の仕様上「有効」として扱われるため、`@NotBlank` だけが発火する。

```xml
<context-param>
    <param-name>jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL</param-name>
    <param-value>true</param-value>
</context-param>
```

---

## JSF required と Bean Validation の二重管理問題

**問題**: `h:inputText` の `required="true"` と `@NotBlank` が両方設定されており、
どちらのメッセージが出るか不定で管理が煩雑だった。

**方針**: `required` 属性をすべて廃止し、Bean Validation に一本化する。
- バリデーションロジック → Bean のフィールドアノテーションで管理
- ラベルの「※必須 / 任意」表示 → 複合コンポーネントの `required` 属性（表示制御のみ）で管理

---

## dads:button にリテラルナビゲーション結果を渡すと ClassCastException

**問題**: `index.xhtml` や `list.xhtml` で `<dads:button action="register" .../>` と書いたら
起動時に `ClassCastException: String cannot be cast to ValueExpression` が発生した。

**原因**: `dads:button` の `action` 属性は composite component インターフェースで
`method-signature="java.lang.String action()"` と宣言されている。
この宣言があると JSF は呼び出し元で渡された値を `MethodExpression` に変換しようとする。
しかしリテラル文字列 `"register"` は `ValueExpression` でも `MethodExpression` でもないため、
`retargetMethodExpressions` フェーズで `String → ValueExpression` キャストに失敗する。

`h:commandButton action="register"` が問題なく動くのは、JSF コアコンポーネントが
リテラル値を特別扱い（固定ナビゲーション結果として処理）するためであり、
composite component 経由では同じ挙動にならない。

**解決策**: フロー入口など「リテラル文字列でナビゲーションしたいだけ」のボタンは
`dads:button` を使わず `h:commandButton` を直接書く。

```xml
<!-- NG: composite component はリテラルを MethodExpression に変換しようとして失敗 -->
<dads:button action="register" ... />

<!-- OK: h:commandButton はリテラルをそのままナビゲーション結果として扱う -->
<h:commandButton action="register"
                 styleClass="dads-button"
                 pt:data-type="solid-fill"
                 pt:data-size="lg" />
```

**適用ルール**: `dads:button` の `action` には必ず EL 式（`#{bean.method}` 形式）を渡すこと。

---

## JSF ライフサイクルと searchAddress() の呼び出し

**前提**: JSF の Ajax で `ajaxExecute` に `postalCode` を指定した場合、
`postalCode` フィールドのバリデーションが失敗すると `searchAddress()` は呼ばれない（Invoke Application フェーズに到達しない）。

**影響**: `searchAddress()` の中でバリデーションを行っても、未入力の場合は到達しない。
→ 未入力チェックは Bean Validation に任せ、`searchAddress()` には正常系のみ書く。
