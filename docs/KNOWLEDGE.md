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

## JSF ライフサイクルと searchAddress() の呼び出し

**前提**: JSF の Ajax で `ajaxExecute` に `postalCode` を指定した場合、
`postalCode` フィールドのバリデーションが失敗すると `searchAddress()` は呼ばれない（Invoke Application フェーズに到達しない）。

**影響**: `searchAddress()` の中でバリデーションを行っても、未入力の場合は到達しない。
→ 未入力チェックは Bean Validation に任せ、`searchAddress()` には正常系のみ書く。
