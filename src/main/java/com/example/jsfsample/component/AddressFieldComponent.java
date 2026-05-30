package com.example.jsfsample.component;

import com.example.jsfsample.model.AddressCandidate;
import com.example.jsfsample.model.AddressFormData;
import com.example.jsfsample.service.AddressSearchService;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 郵便番号検索＋住所入力カスタムコンポーネント。
 * UIInput を継承して BV 連鎖に対応し、NamingContainer として子コンポーネントのIDを隔離する。
 * レンダリングは addressField.xhtml（composite component）が担当する。
 * 状態は getStateHelper() を通じて JSF のビューステートに保存する。
 */
@FacesComponent("com.example.jsfsample.component.AddressFieldComponent")
public class AddressFieldComponent extends UIInput implements NamingContainer {

    private enum Key { candidates, showModal, selectedIndex, errorMessage }

    @Override
    public String getFamily() {
        return UINamingContainer.COMPONENT_FAMILY;
    }

    // ---- 住所検索アクション ----

    public String search(AddressFormData addressFormData) {
        AddressSearchService service = CDI.current().select(AddressSearchService.class).get();
        List<AddressCandidate> results = service.search(addressFormData.getPostalCode());

        setErrorMessage(null);

        if (results.isEmpty()) {
            setErrorMessage("該当する住所が見つかりませんでした");
            setCandidates(Collections.emptyList());
            setShowModal(false);
        } else if (results.size() == 1) {
            apply(addressFormData, results.get(0));
            setCandidates(Collections.emptyList());
            setShowModal(false);
        } else {
            setCandidates(results);
            setShowModal(true);
            setSelectedIndex(0);
        }
        return null;
    }

    public String confirmSelection(AddressFormData addressFormData) {
        List<AddressCandidate> list = getCandidates();
        int idx = getSelectedIndex();
        if (list != null && idx >= 0 && idx < list.size()) {
            apply(addressFormData, list.get(idx));
        }
        setCandidates(Collections.emptyList());
        setShowModal(false);
        return null;
    }

    public String cancelModal() {
        setShowModal(false);
        return null;
    }

    private void apply(AddressFormData addr, AddressCandidate c) {
        addr.setPrefecture(c.getPrefecture());
        addr.setCity(c.getCity());
        addr.setStreetAddress(c.getStreetAddress());
    }

    // ---- f:ajax execute ターゲット生成 ----

    /** 検索ボタンの execute に渡す：郵便番号入力のみを処理対象にする */
    public String getSearchExecute() {
        return getClientId(FacesContext.getCurrentInstance()) + ":postalCode";
    }

    /** 確定ボタンの execute に渡す：ラジオボタンのみを処理対象にする */
    public String getConfirmExecute() {
        return getClientId(FacesContext.getCurrentInstance()) + ":candidateSelect";
    }

    // ---- 候補リスト → SelectItem 変換 ----

    public List<SelectItem> getCandidateSelectItems() {
        List<AddressCandidate> list = getCandidates();
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return IntStream.range(0, list.size())
                .mapToObj(i -> new SelectItem(i, list.get(i).getFullAddress()))
                .toList();
    }

    // ---- 状態アクセサ（getStateHelper 経由でビューステートに保存） ----

    @SuppressWarnings("unchecked")
    public List<AddressCandidate> getCandidates() {
        List<AddressCandidate> list = (List<AddressCandidate>) getStateHelper().get(Key.candidates);
        return list != null ? list : Collections.emptyList();
    }

    public void setCandidates(List<AddressCandidate> candidates) {
        getStateHelper().put(Key.candidates, candidates);
    }

    public boolean isShowModal() {
        return Boolean.TRUE.equals(getStateHelper().eval(Key.showModal, Boolean.FALSE));
    }

    public void setShowModal(boolean show) {
        getStateHelper().put(Key.showModal, show);
    }

    public int getSelectedIndex() {
        Number v = (Number) getStateHelper().eval(Key.selectedIndex, 0);
        return v.intValue();
    }

    public void setSelectedIndex(int idx) {
        getStateHelper().put(Key.selectedIndex, idx);
    }

    public String getErrorMessage() {
        return (String) getStateHelper().get(Key.errorMessage);
    }

    public void setErrorMessage(String msg) {
        getStateHelper().put(Key.errorMessage, msg);
    }
}
