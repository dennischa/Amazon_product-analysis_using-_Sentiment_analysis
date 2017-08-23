# kmucs-bigdata-project-student8-winston


external library 주소
1. 단어감정점수 사전
http://sentiwordnet.isti.cnr.it/
2. stanford coreNLP(자연어처리)
https://stanfordnlp.github.io/CoreNLP/
3. slide link
https://www.slideshare.net/kyungjuncha/amazon-product-analysis-using-sentiment-analysis

에서 다운로드하여 사용하였습니다.

github에는 sample data(review, metadata)를 사용한 결과를 올렸습니다.
salesRank가 없는 경우에는 -1의 값을
reviewText가 없는 경우에는 감정점수를 매길 수 없으므로 0.0이 들어가게 됩니다.

샘플데이타의 경우 대부분의 상품들이 중복되지않아 review와 metadata가 중복되는 몇몇 상품만 두 점수가 정상적으로 입력됬습니다.
