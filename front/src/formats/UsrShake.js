import {Quill} from "@vueup/vue-quill";

const Embed = Quill.import('blots/embed');

export class UsrShake extends Embed {
    static blotName = "shake"
    static tagName = "span"
    static className = "usr-shake"

    static create(value) {
        let node = super.create();
        // node.setAttribute('contenteditable', 'false');
        if (typeof value === 'string') {
            node.innerHTML = value.replace(/(.) */g, "<span>$1</span>");
        }
        return node;
    }

    static formats() {
        return true;
    }

    static value(domNode) {
        return domNode.innerText.replace(/<\/?span[^>]*>/g, "");
    }

}
