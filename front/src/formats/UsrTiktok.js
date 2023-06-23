// import Inline from "quill/blots/inline";
import {Quill} from "@vueup/vue-quill";

const Inline = Quill.import('blots/inline');

export class UsrTiktok extends Inline {
    static blotName = "tiktok"
    static tagName = "span"
    static className = "usr-tiktok"

    static formats() {
        return true;
    }
}
